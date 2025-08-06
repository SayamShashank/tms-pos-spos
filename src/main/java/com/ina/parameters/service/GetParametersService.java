package com.ina.parameters.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ina.common.crypto.service.DataDecryptionService;
import com.ina.common.crypto.service.DataEncryptionService;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiOutContext;
import com.ina.common.model.SecureRespMetadata;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.utils.CommonUtils;
import com.ina.common.utils.HashUtils;
import com.ina.common.validator.CommonValidator;
import com.ina.common.validator.DeviceProfileValidator;
import com.ina.config.RequestPropertyConfig;
import com.ina.dao.EMVParametersRepository;
import com.ina.dao.entity.EMVParameters;
import com.ina.parameters.messages.Acknowledgement;
import com.ina.parameters.messages.ParameterDownload;
import com.ina.parameters.messages.Registration;
import com.ina.parameters.model.*;
import com.ina.parameters.utils.HttpClient;
import com.ina.parameters.utils.JsonMapperUtil;
import com.ina.parameters.utils.Marshal;
import com.ina.tms.packages.xml.v8.catm118.Document;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static com.ina.common.constants.AppErrorConstants.SUCCESS_CODE;
import static com.ina.constants.AppConstants.TMS;
import static com.ina.parameters.utils.JsonMapperUtil.getResult;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Slf4j
public class GetParametersService extends CommonValidator<GetParametersRequest> {

    private final InaPayMessages inaPayMessages;
    private final EMVParametersRepository emvParametersRepository;
    private final ObjectMapper objectMapper;
    private final DataEncryptionService dataEncryptionService;
    private final DataDecryptionService dataDecryptionService;
    private final DeviceProfileValidator deviceProfileValidator;
    private final RequestPropertyConfig requestPropertyConfig;
    @Getter
    @Setter
    private Object respObj;
    private final HashUtils hashUtils;
    private final HttpClient httpClient;
    private final Marshal marshal;
    private static final String REJECT_NS_URI = "urn:iso:std:iso:20022:tech:xsd:catm.004.001.04";
    private static final String MGMT_PLAN_NS_URI = "urn:iso:std:iso:20022:tech:xsd:catm.002.001.07";
    private static final String ACCEPT_CONFIG_UPDATE_NS_URI = "urn:iso:std:iso:20022:tech:xsd:catm.003.001.08";

    public GetParametersService(InaPayMessages inaPayMessages, EMVParametersRepository emvParametersRepository, ObjectMapper objectMapper, DataEncryptionService dataEncryptionService, DataDecryptionService dataDecryptionService, DeviceProfileValidator deviceProfileValidator, RequestPropertyConfig requestPropertyConfig, HashUtils hashUtils, HttpClient httpClient, Marshal marshal) {
        super(inaPayMessages);
        this.inaPayMessages = inaPayMessages;
        this.emvParametersRepository = emvParametersRepository;
        this.objectMapper = objectMapper;
        this.dataEncryptionService = dataEncryptionService;
        this.dataDecryptionService = dataDecryptionService;
        this.deviceProfileValidator = deviceProfileValidator;
        this.requestPropertyConfig = requestPropertyConfig;
        this.hashUtils = hashUtils;
        this.httpClient = httpClient;
        this.marshal = marshal;
    }


    public ParameterSecureResponse getParameters(GetParametersRequest request) throws JsonProcessingException {
        evaluate(request);
        String inputRefId = request.getApiInContext().getInputRefId();
        String decryptedData = dataDecryptionService.decryptData(request.getSecureReqMetadata(), TMS,
                request.getDeviceMetadata().getDeviceId(), inputRefId);
        GetParametersRequestData requestData = objectMapper.readValue(decryptedData, GetParametersRequestData.class);
        deviceProfileValidator.validateDecryptedRequest(
                requestData.getApiInContext().getInputRefId(),
                requestData.getDeviceMetadata().getDeviceId(),
                request.getApiInContext(), request.getDeviceMetadata());
        EMVParameters savedEmvParams = getEmvParameters(requestData);
        TmsParams emvParameters = registerParameterAck(requestData, savedEmvParams);
        return getParameterSecureResponse(emvParameters, request, requestData);
    }

    @NotNull
    private ParameterSecureResponse getParameterSecureResponse(TmsParams emvParameters, GetParametersRequest request, GetParametersRequestData requestData) throws JsonProcessingException {
        EMVParameters checksumParams = getEmvParameters(requestData);
        ParameterSecureResponse response = new ParameterSecureResponse();
        ApiOutContext apiOutContext = CommonUtils.getApiOutContext(request.getApiInContext().getInputRefId(), SUCCESS_CODE , inaPayMessages, inaPayMessages.get(SUCCESS_CODE));
        ParameterResponse parameterResponse = new ParameterResponse();
        parameterResponse.setApiOutContext(apiOutContext);
        log.info("Retrieved EMV parameters:{}", emvParameters);
        if (nonNull(emvParameters) && CollectionUtils.isNotEmpty(emvParameters.getAids())) {
            parameterResponse.setEmvParameters(emvParameters);
        } else if (isNull(emvParameters)&&nonNull(checksumParams)){
            List<AidData> aidLists = objectMapper.readValue(checksumParams.getAids(), new TypeReference<List<AidData>>() {
                    }
            );
            String aids = objectMapper.writeValueAsString(aidLists);
            log.info("Aids:{}", aids);
            List<RidData> ridLists = objectMapper.readValue(checksumParams.getCpks(), new TypeReference<List<RidData>>() {
            });
            String cpks = objectMapper.writeValueAsString(ridLists);
            log.info("cpks:{}", cpks);
            MerchantDetails merchantDetailsFromDb = objectMapper.readValue(checksumParams.getMerchantDetails(), MerchantDetails.class);
            String merchantDetails = objectMapper.writeValueAsString(merchantDetailsFromDb);
            log.info("merchantDetails:{}", merchantDetails);
            MerchantTerminalData terminalData = objectMapper.readValue(checksumParams.getTerminalConfig(), MerchantTerminalData.class);
            String terminalConfig = objectMapper.writeValueAsString(terminalData);
            log.info("TerminalConfig:{}", terminalConfig);
            TmsParams tmsParams = new TmsParams();
            tmsParams.setAids(aidLists);
            tmsParams.setCpks(ridLists);
            tmsParams.setMerchantDetails(merchantDetailsFromDb);
            tmsParams.setTerminalConfig(terminalData);
            String tmsParamsResponse = objectMapper.writeValueAsString(tmsParams);
            log.info("tmsParamsResponse:{}", tmsParamsResponse);
            parameterResponse.setEmvParameters(tmsParams);
            log.info("param response:{}", parameterResponse);
        }

        else {
            log.info("EMV parameters is null and executing with static parameters:{}", getData());
            JsonNode root = objectMapper.readTree(getData());
            log.info("root parameters:{}", root.toString());
            JsonNode emvParametersNode = root.get("emvParameters");
            String emvParams = objectMapper.writeValueAsString(emvParametersNode);
            log.info("emv parameters:{}", emvParams);
            TmsParams params = objectMapper.readValue(emvParams, TmsParams.class);
            log.info("tms parameters:{}", params);
            String cpks = objectMapper.writeValueAsString(params.getCpks());
            String aids = objectMapper.writeValueAsString(params.getAids());
            String merchantDetails = objectMapper.writeValueAsString(params.getMerchantDetails());
            String terminalConfig = objectMapper.writeValueAsString(params.getTerminalConfig());
            if (isNull(checksumParams)) {
                checksumParams = EMVParameters.builder()
                        .terminalId(requestData.getRequestData().getTid())
                        .merchantId(params.getTerminalConfig().getMerchantId())
                        .deviceId(request.getDeviceMetadata().getDeviceId())
                        .cpks(cpks)
                        .aids(aids)
                        .merchantDetails(merchantDetails)
                        .terminalConfig(terminalConfig)
                        .createdDate(Timestamp.valueOf(LocalDateTime.now()))
                        .updatedDate(Timestamp.valueOf(LocalDateTime.now()))
                        .build();
            }
            parameterResponse.setEmvParameters(params);
        }
        String data = objectMapper.writeValueAsString(parameterResponse);
        String checksum = hashUtils.generateSHA512(data);
        checksumParams.setParamCheckSum(checksum);
        emvParametersRepository.save(checksumParams);
        log.info("paramCheckSum:{}", checksum);
        SecureRespMetadata secureRespMetadata = getSecureRespMetadata(request, data);
        response.setSecureRespMetadata(secureRespMetadata);
        response.setApiOutContext(apiOutContext);
        return response;
    }

    private SecureRespMetadata getSecureRespMetadata(GetParametersRequest request, String data) {
        return dataEncryptionService.encryptData(data, TMS, request.getDeviceMetadata().getDeviceId(), request.getApiInContext().getInputRefId());
    }

    private <T> String exchange(T classObj) throws JsonProcessingException {

        marshal.setUseCustomWriter(Boolean.FALSE);
        marshal.setAddNs(Boolean.TRUE);
        String stsRptMsg = marshal.marshalToXml(classObj, classObj.getClass().getSimpleName());
        stsRptMsg = stsRptMsg.replace("<xsi:Document", "<Document").replace("</xsi:Document>", "</Document>");
        log.info("req: ");
        log.info(stsRptMsg);


        String ep = "statusReport.htm";
        String respXmlData = httpClient.exchange(ep, stsRptMsg);

        if (respXmlData != null) {

            log.info("resp: ");
            String resXmlData = objectMapper.writeValueAsString(respXmlData);
            log.info("respXmlData:{}", resXmlData);
            log.info("resp End");
            Object xmlRespObj = marshal.parseXml(respXmlData);
            String value = objectMapper.writeValueAsString(xmlRespObj);
            log.info("xmlRespObj:{}", value);

            this.setRespObj(xmlRespObj);
            return marshal.getExpectedNsUri();

        }
        return null;
    }

    public Boolean registration(GetParametersRequestData request) throws JsonProcessingException {


        Registration registration = getRegistration();
        Document doc = registration.genStsRpt(request.getRequestData());

        log.info("registration: ");

        String expectedNsUri = exchange(doc);
        if (expectedNsUri != null) {
            if (expectedNsUri.equals(MGMT_PLAN_NS_URI)) {
                com.ina.tms.packages.xml.v8.catm217.Document mgmtPlanDoc = (com.ina.tms.packages.xml.v8.catm217.Document) this
                        .getRespObj();
                log.info("mgmt plan replace: ");
                log.info(mgmtPlanDoc.getMgmtPlanRplcmnt().getMgmtPlan().getDataSet()
                        .getCntt().getActn().getFirst().getDataSetId().getNm());
                com.ina.tms.packages.xml.v8.catm217.DataSetCategory12Code dsiType = mgmtPlanDoc.getMgmtPlanRplcmnt()
                        .getMgmtPlan().getDataSet().getCntt().getActn().getFirst().getDataSetId().getTp();
                if (dsiType.value().equals("Parameters")) {
                    return true;
                }
            } else if (expectedNsUri.equals(REJECT_NS_URI)) {
//
            }
            return true;
        }
        return false;
    }

    @NotNull
    private Registration getRegistration() {
        return new Registration(requestPropertyConfig);
    }

    public void acknowledgment(GetParametersRequestData request) throws JsonProcessingException {

        Acknowledgement acknowledgement = new Acknowledgement(requestPropertyConfig);
        Document doc = acknowledgement.genStsRpt(request.getRequestData());

        log.info("param dld ack: ");
        this.exchange(doc);
    }

    public ParameterResult paramDownload(GetParametersRequestData request, EMVParameters savedEmvParams) throws JsonProcessingException {

        ParameterDownload paramDld = getParameterDownload();
        Document doc = paramDld.genStsRpt(request.getRequestData());

        log.info("param dld param: ");
        String expectedNsUri = exchange(doc);
        TmsParams tmsParams = new TmsParams();
        if (expectedNsUri != null) {
            if (expectedNsUri.equals(ACCEPT_CONFIG_UPDATE_NS_URI)) {
                log.info("param success");
                EMVParameters params = getParams(request, tmsParams, savedEmvParams);
                return new ParameterResult(true, tmsParams, params);
            }
            return new ParameterResult(true, tmsParams, savedEmvParams);
        }
        return new ParameterResult(true, tmsParams, savedEmvParams);
    }

    private EMVParameters getParams(GetParametersRequestData request, TmsParams tmsParams, EMVParameters savedEmvParams) throws JsonProcessingException {
        com.ina.tms.packages.xml.v8.catm318.Document acptrConfigUpdateDoc = (com.ina.tms.packages.xml.v8.catm318.Document) this
                .getRespObj();
        List<String> merchParams = acptrConfigUpdateDoc.getAccptrCfgtnUpd()
                .getAccptrCfgtn()
                .getDataSet()
                .stream()
                .flatMap(dataset -> dataset.getCntt().getMrchntParams().stream()
                        .map(param -> {
                            byte[] othrParams = param.getOthrParams();
                            return new String(Base64.decodeBase64(Base64.encodeBase64String(othrParams)));
                        }))
                .toList();
        List<String> appParamsList = acptrConfigUpdateDoc.getAccptrCfgtnUpd()
                .getAccptrCfgtn()
                .getDataSet()
                .stream()
                .flatMap(dataset -> dataset.getCntt()
                        .getApplParams()
                        .stream()
                        .flatMap(applicationParams -> applicationParams.getParams()
                                .stream()
                                .map(param -> new String(Base64.decodeBase64(Base64.encodeBase64String(param))))))
                .toList();
        JsonMapperUtil.Result result = getResult(appParamsList, merchParams);
        tmsParams.setAids(result.aidLists());
        tmsParams.setCpks(result.ridList());
        tmsParams.setTerminalConfig(result.terminalConfig());
        tmsParams.setMerchantDetails(result.merchantDetails());
        String parameterDownload = objectMapper.writeValueAsString(tmsParams);
        log.info("Fetched Parameters:{}", parameterDownload);
        if (isNull(savedEmvParams)) {
            EMVParameters savedEmvParameters = EMVParameters.builder()
                    .deviceId(request.getDeviceMetadata().getDeviceId())
                    .merchantId(result.terminalConfig().getMerchantId())
                    .terminalId(result.terminalConfig().getTerminalId())
                    .trsMid(request.getRequestData().getTrsMid())
                    .aids(objectMapper.writeValueAsString(result.aidLists()))
                    .cpks(objectMapper.writeValueAsString(result.ridList()))
                    .terminalConfig(objectMapper.writeValueAsString(result.terminalConfig()))
                    .merchantDetails(objectMapper.writeValueAsString(result.merchantDetails()))
                    .createdDate(Timestamp.valueOf(LocalDateTime.now()))
                    .updatedDate(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
            return emvParametersRepository.save(savedEmvParameters);
        } else {

            savedEmvParams.setAids(objectMapper.writeValueAsString(result.aidLists()));
            savedEmvParams.setCpks(objectMapper.writeValueAsString(result.ridList()));
            savedEmvParams.setTerminalConfig(objectMapper.writeValueAsString(result.terminalConfig()));
            savedEmvParams.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
            return emvParametersRepository.save(savedEmvParams);
        }

    }

    @NotNull
    protected ParameterDownload getParameterDownload() {
        return new ParameterDownload(requestPropertyConfig);
    }

    private EMVParameters getEmvParameters(GetParametersRequestData request) {
        return emvParametersRepository.findByTrsMidAndTerminalIdAndDeviceId(request.getRequestData().getTrsMid(), request.getRequestData().getTid(), request.getDeviceMetadata().getDeviceId());
    }



    public record ParameterResult(boolean isPerformed, TmsParams parameters,EMVParameters emvParameters){}




    public TmsParams registerParameterAck(GetParametersRequestData request, EMVParameters savedEmvParams) throws JsonProcessingException {
        if (isNull(savedEmvParams)) {
            if (Boolean.TRUE.equals(registration(request))) {
                ParameterResult parameterResult = extractParameters(request, null);
                if (nonNull(parameterResult) && parameterResult.isPerformed) {
                    acknowledgment(request);
                    return parameterResult.parameters;
                }
            }
        } else {
            ParameterResult parameterResult = extractParameters(request, savedEmvParams);
            if (nonNull(parameterResult) && parameterResult.isPerformed) {
                acknowledgment(request);
                return parameterResult.parameters;
            }
        }

        return null;
    }

    private ParameterResult extractParameters(GetParametersRequestData request,EMVParameters savedEmvParams) throws JsonProcessingException {
        return paramDownload(request,savedEmvParams);
    }


    @NotNull
    private static String getData() {
        return "{\"apiOutContext\":{\"timeStamp\":\"2025-07-28T13:18:35.272235100\",\"outputRefId\":\"6c5af666-d44b-4d63-9c5f-d9de456daee2\",\"code\":\"TMS0000\",\"status\":\"Success\",\"message\":\"Success\"},\"emvParameters\":{\"aids\":[{\"aid\":\"A000000333010103\",\"applicationName\":\"UPI\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"D84004F800\",\"tacDefault\":\"D84000A800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A000000333010101\",\"applicationName\":\"UPI\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"D84004F800\",\"tacDefault\":\"D84000A800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A000000333010102\",\"applicationName\":\"UPI\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"D84004F800\",\"tacDefault\":\"D84000A800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A0000000041010\",\"applicationName\":\"MASTERCARD GN\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0040000000\",\"tacOnline\":\"F850ACF800\",\"tacDefault\":\"F850ACA000\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A0000000032010\",\"applicationName\":\"Visa Electron\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"D84004F800\",\"tacDefault\":\"D84000A800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A0000000031010\",\"applicationName\":\"VISA CREDIT\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"D84004F800\",\"tacDefault\":\"D84004A800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A0000000033010\",\"applicationName\":\"Visa Interlink\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"DC4004F800\",\"tacDefault\":\"DC4000A800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A0000002282010\",\"applicationName\":\"SPAN VSDC\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"FC408CF800\",\"tacDefault\":\"FC408CA800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000100000\",\"transactionLimit\":\"000000010000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A0000002281010\",\"applicationName\":\"SPAN MCHIP\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"FC408CF800\",\"tacDefault\":\"FC408CA800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000010000\",\"transactionLimit\":\"000000010000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A0000000031010\",\"applicationName\":\"VISA CREDIT\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"D84004F800\",\"tacDefault\":\"D84004A800\",\"cvmLimit\":\"000000000300\",\"floorLimit\":\"000000000075\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A0000000032010\",\"applicationName\":\"Visa Electron\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"DC4004F800\",\"tacDefault\":\"DC4000A800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A0000000033010\",\"applicationName\":\"Visa Interlink\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"DC4004F800\",\"tacDefault\":\"DC4000A800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A000000025010402\",\"applicationName\":\"AMEX\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0000000000\",\"tacOnline\":\"CC00FC8000\",\"tacDefault\":\"CC00FC8000\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A00000002501\",\"applicationName\":\"AMEX\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"DE00FC9800\",\"tacDefault\":\"DC50FC9800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null},{\"aid\":\"A000000025\",\"applicationName\":\"AMEX\",\"cardSchemeAcquirerId\":\"RYDB\",\"securityCapability\":null,\"addTerminalCapability\":null,\"terminalCapability\":\"F000F0A001\",\"tacDenial\":\"0000000000\",\"tacOnline\":\"0000000000\",\"tacDefault\":\"0000000000\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null}],\"cpks\":[{\"rid\":\"A000000333\",\"keyId\":\"0B\",\"rsaArithmeticIndex\":\"01\",\"module\":\"CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157\",\"expiry\":\"301231\",\"checksum\":\"BD331F9996A490B33C13441066A09AD3FEB5F66C\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"0A\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EF\",\"expiry\":\"301231\",\"checksum\":\"C88BE6B2417C4F941C9371EA35A377158767E4E3\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"08\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BF\",\"expiry\":\"301231\",\"checksum\":\"EE23B616C95C02652AD18860E48787C079E8E85A\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"09\",\"rsaArithmeticIndex\":\"01\",\"module\":\"EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5\",\"expiry\":\"301231\",\"checksum\":\"A075306EAB0045BAF72CDD33B3B678779DE1F527\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"BF\",\"rsaArithmeticIndex\":\"01\",\"module\":\"C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3A97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E3\",\"expiry\":\"301231\",\"checksum\":\"6BDA32B1AA171444C7E8F88075A74FBFE845765F\",\"exponent\":\"03\"},{\"rid\":\"A000000004\",\"keyId\":\"F1\",\"rsaArithmeticIndex\":\"01\",\"module\":\"A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7\",\"expiry\":\"301231\",\"checksum\":\"D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB\",\"exponent\":\"03\"},{\"rid\":\"A000000004\",\"keyId\":\"EF\",\"rsaArithmeticIndex\":\"01\",\"module\":\"A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F899EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DCF5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE00DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651AB86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFFDA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3B\",\"expiry\":\"301231\",\"checksum\":\"21766EBB0EE122AFB65D7845B73DB46BAB65427A\",\"exponent\":\"03\"},{\"rid\":\"A000000004\",\"keyId\":\"05\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597\",\"expiry\":\"301231\",\"checksum\":\"EBFA0D5D06D8CE702DA3EAE890701D45E274C845\",\"exponent\":\"03\"},{\"rid\":\"A000000004\",\"keyId\":\"FE\",\"rsaArithmeticIndex\":\"01\",\"module\":\"A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A1\",\"expiry\":\"301231\",\"checksum\":\"9A295B05FB390EF7923F57618A9FDA2941FC34E0\",\"exponent\":\"03\"},{\"rid\":\"A000000003\",\"keyId\":\"94\",\"rsaArithmeticIndex\":\"01\",\"module\":\"ACD2B12302EE644F3F835ABD1FC7A6F62CCE48FFEC622AA8EF062BEF6FB8BA8BC68BBF6AB5870EED579BC3973E121303D34841A796D6DCBC41DBF9E52C4609795C0CCF7EE86FA1D5CB041071ED2C51D2202F63F1156C58A92D38BC60BDF424E1776E2BC9648078A03B36FB554375FC53D57C73F5160EA59F3AFC5398EC7B67758D65C9BFF7828B6B82D4BE124A416AB7301914311EA462C19F771F31B3B57336000DFF732D3B83DE07052D730354D297BEC72871DCCF0E193F171ABA27EE464C6A97690943D59BDABB2A27EB71CEEBDAFA1176046478FD62FEC452D5CA393296530AA3F41927ADFE434A2DF2AE3054F8840657A26E0FC617\",\"expiry\":\"301231\",\"checksum\":\"C4A3C43CCF87327D136B804160E47D43B60E6E0F\",\"exponent\":\"03\"},{\"rid\":\"A000000004\",\"keyId\":\"FE\",\"rsaArithmeticIndex\":\"01\",\"module\":\"A653EAC1C0F786C8724F737F172997D63D1C3251C44402049B865BAE877D0F398CBFBE8A6035E24AFA086BEFDE9351E54B95708EE672F0968BCD50DCE40F783322B2ABA04EF137EF18ABF03C7DBC5813AEAEF3AA7797BA15DF7D5BA1CBAF7FD520B5A482D8D3FEE105077871113E23A49AF3926554A70FE10ED728CF793B62A1\",\"expiry\":\"301231\",\"checksum\":\"9A295B05FB390EF7923F57618A9FDA2941FC34E0\",\"exponent\":\"03\"},{\"rid\":\"A000000004\",\"keyId\":\"05\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597\",\"expiry\":\"301231\",\"checksum\":\"EBFA0D5D06D8CE702DA3EAE890701D45E274C846\",\"exponent\":\"03\"},{\"rid\":\"A000000004\",\"keyId\":\"05\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B8048ABC30C90D976336543E3FD7091C8FE4800DF820ED55E7E94813ED00555B573FECA3D84AF6131A651D66CFF4284FB13B635EDD0EE40176D8BF04B7FD1C7BACF9AC7327DFAA8AA72D10DB3B8E70B2DDD811CB4196525EA386ACC33C0D9D4575916469C4E4F53E8E1C912CC618CB22DDE7C3568E90022E6BBA770202E4522A2DD623D180E215BD1D1507FE3DC90CA310D27B3EFCCD8F83DE3052CAD1E48938C68D095AAC91B5F37E28BB49EC7ED597\",\"expiry\":\"301231\",\"checksum\":\"EBFA0D5D06D8CE702DA3EAE890701D45E274C845\",\"exponent\":\"03\"},{\"rid\":\"A000000004\",\"keyId\":\"EF\",\"rsaArithmeticIndex\":\"01\",\"module\":\"A191CB87473F29349B5D60A88B3EAEE0973AA6F1A082F358D849FDDFF9C091F899EDA9792CAF09EF28F5D22404B88A2293EEBBC1949C43BEA4D60CFD879A1539544E09E0F09F60F065B2BF2A13ECC705F3D468B9D33AE77AD9D3F19CA40F23DCF5EB7C04DC8F69EBA565B1EBCB4686CD274785530FF6F6E9EE43AA43FDB02CE00DAEC15C7B8FD6A9B394BABA419D3F6DC85E16569BE8E76989688EFEA2DF22FF7D35C043338DEAA982A02B866DE5328519EBBCD6F03CDD686673847F84DB651AB86C28CF1462562C577B853564A290C8556D818531268D25CC98A4CC6A0BDFFFDA2DCCA3A94C998559E307FDDF915006D9A987B07DDAEB3B\",\"expiry\":\"301231\",\"checksum\":\"21766EBB0EE122AFB65D7845B73DB46BAB65427A\",\"exponent\":\"03\"},{\"rid\":\"A000000004\",\"keyId\":\"F1\",\"rsaArithmeticIndex\":\"01\",\"module\":\"A0DCF4BDE19C3546B4B6F0414D174DDE294AABBB828C5A834D73AAE27C99B0B053A90278007239B6459FF0BBCD7B4B9C6C50AC02CE91368DA1BD21AAEADBC65347337D89B68F5C99A09D05BE02DD1F8C5BA20E2F13FB2A27C41D3F85CAD5CF6668E75851EC66EDBF98851FD4E42C44C1D59F5984703B27D5B9F21B8FA0D93279FBBF69E090642909C9EA27F898959541AA6757F5F624104F6E1D3A9532F2A6E51515AEAD1B43B3D7835088A2FAFA7BE7\",\"expiry\":\"301231\",\"checksum\":\"D8E68DA167AB5A85D8C3D55ECB9B0517A1A5B4BB\",\"exponent\":\"03\"},{\"rid\":\"A000000228\",\"keyId\":\"19\",\"rsaArithmeticIndex\":\"01\",\"module\":\"A856777B6B4AF4D1C5D8E955852D57539FAC9B51B7879E3AC99A9F9B32F9A9DE267D6FB3BFAB54A4C4955EF90A5C4561C714A5F1E57550D33B320FF9238CE703CD4834712E32A3AC09B11320DFECFD885CF58EAAEAE2462FA0F194CCA29F0FF3431653DDD30B51101D98ED4377E3AC6B525AAB8D2023804C8724B3A98A4E94D3AE358FA1FC05E4A8DCFEFCFB5E930834D1E94AA665F923F40CDB06C3ABF213165ADE547E67A2800FD15D32EE42FCC30A07F1F6709E4984AE55A7DB79D4EBE184392358F1CBFE1D7C772A62954759AB7BA563EF4E09B72A961D19C2B5870EEC69F6E28493FD3EF0CF8F97FC6584B75696675045015DB4AA95\",\"expiry\":\"301231\",\"checksum\":\"ABCDEFERFGEGEDEWEEDFGGGEDEDDDFFFGGGGREED\",\"exponent\":\"03\"},{\"rid\":\"A000000228\",\"keyId\":\"29\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B6D73BF68564C88A1AEE8BA70A5F60CE495CA722E097DADEEBB83B28040B1BAD16DBC9AC3CD181BA89193638E600AF397D220F0339A8E792AA08C1878482ACC463B3B3A257AE8667CDBC1D6613CB9CBB612830FDA7F7BA689A148EFFF34476F6E0A70C819C10B3B6150909B58BF9403F5BB2E9790EE82C50C8D6FB267C726DC255AE97FABF5A357B2A0FBD1387168D83B25ECD912027B3868F072E025240CF780CC8E5839823727E5547FD1366A203F4F70FA82660B8401D4D2D06FD9A4036D14C53F6289D6FDC724E7D06F31ED93AC1B54083D9B9FCF09B135FDE9F4F6C1F0BA0142C3715E49015958C45315859DB12D942D75497FB51ED\",\"expiry\":\"301231\",\"checksum\":\"08374162F808F8DAD0CECB8FCD1AF5F64F213D6F\",\"exponent\":\"03\"},{\"rid\":\"A000000228\",\"keyId\":\"33\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B40F44F511A2F54000A3326EDBF0E1CD2C4E8EF429A6C28C2014D5066488EE1D8EEF92F816690FEEDA4961DE12EDB366D6008D90CFBB72C1314F6FA37FD7BD8D96C9D61420AAC638862C1FF0A42C0678D3D6FB2DCBA4282D2BA78831899F6FE5F34A3086F959EAC6C734CCE18064F69B68E36901199E265631EB0129F17FFD175BB4EA59D84AFB916EC609492FB1AAD7431A6748F85B95686243E59A71D4E9332904D1FA16A134E52C85E59D6C26AE6A4EA32ED9FD1D0EFFACC6B3D2A7E102AA61BDD375594799BC7D41884C3F8F6C7D0125FC7C3AC13E4DF7488FC15352CC0953E6FDF073C2127AB7BA0E5FDB7C1B2852B5C3AEC89F89C5\",\"expiry\":\"301231\",\"checksum\":\"ED535A86C7A766CDAA07EA8766C7A2B688B7F4F5\",\"exponent\":\"03\"},{\"rid\":\"A000000228\",\"keyId\":\"20\",\"rsaArithmeticIndex\":\"01\",\"module\":\"EBA2E3FBE75D51C519B7A498CFE53F51519B292C1BBE0C78C14FBE38E3717DE0C0ECC04605879EF617B97ED1E8E989FCD2C7DDF61EE09E96F7EADA7D9F553426D6D6A8BE4DCF943D6C8F3627265520F757BBA16FF68749F3D796A0AAACA0ED0929BE112BF7CAB87BED4D9B5DD9B7A0CAC7F9CA513A6BFC03B4C20EFDC03E2B58D76E2ABF466665CB9D64AA412FBBF85259C480DA2F0896AB28FBB26022EFAE74CCDB9C36749E8D29AE4069A1298A0B07A7F72DFF8E6F442A2393DFF7E4E1F06F\",\"expiry\":\"301231\",\"checksum\":\"48AE1E709DBBBDC9CEED5B5FEEE233CD1248CA70\",\"exponent\":\"03\"},{\"rid\":\"A000000228\",\"keyId\":\"99\",\"rsaArithmeticIndex\":\"03\",\"module\":\"B6D73BF68564C88A1AEE8BA70A5F60CE495CA722E097DADEEBB83B28040B1BAD16DBC9AC3CD181BA89193638E600AF397D220F0339A8E792AA08C1878482ACC463B3B3A257AE8667CDBC1D6613CB9CBB612830FDA7F7BA689A148EFFF34476F6E0A70C819C10B3B6150909B58BF9403F5BB2E9790EE82C50C8D6FB267C726DC255AE97FABF5A357B2A0FBD1387168D83B25ECD912027B3868F072E025240CF780CC8E5839823727E5547FD1366A203F4F70FA82660B8401D4D2D06FD9A4036D14C53F6289D6FDC724E7D06F31ED93AC1B54083D9B9FCF09B135FDE9F4F6C1F0BA0142C3715E49015958C45315859DB12D942D75497FB51ED\",\"expiry\":\"301231\",\"checksum\":\"08374162F808F8DAD0CECB8FCD1AF5F64F213D6F\",\"exponent\":\"03\"},{\"rid\":\"A000000228\",\"keyId\":\"90\",\"rsaArithmeticIndex\":\"02\",\"module\":\"B6D73BF68564C88A1AEE8BA70A5F60CE495CA722E097DADEEBB83B28040B1BAD16DBC9AC3CD181BA89193638E600AF397D220F0339A8E792AA08C1878482ACC463B3B3A257AE8667CDBC1D6613CB9CBB612830FDA7F7BA689A148EFFF34476F6E0A70C819C10B3B6150909B58BF9403F5BB2E9790EE82C50C8D6FB267C726DC255AE97FABF5A357B2A0FBD1387168D83B25ECD912027B3868F072E025240CF780CC8E5839823727E5547FD1366A203F4F70FA82660B8401D4D2D06FD9A4036D14C53F6289D6FDC724E7D06F31ED93AC1B54083D9B9FCF09B135FDE9F4F6C1F0BA0142C3715E49015958C45315859DB12D942D75497FB51ED\",\"expiry\":\"251031\",\"checksum\":\"08374162F808F8DAD0CECB8FCD1AF5F64F213D6F\",\"exponent\":\"02\"},{\"rid\":\"A000000228\",\"keyId\":\"90\",\"rsaArithmeticIndex\":\"02\",\"module\":\"B6D73BF68564C88A1AEE8BA70A5F60CE495CA722E097DADEEBB83B28040B1BAD16DBC9AC3CD181BA89193638E600AF397D220F0339A8E792AA08C1878482ACC463B3B3A257AE8667CDBC1D6613CB9CBB612830FDA7F7BA689A148EFFF34476F6E0A70C819C10B3B6150909B58BF9403F5BB2E9790EE82C50C8D6FB267C726DC255AE97FABF5A357B2A0FBD1387168D83B25ECD912027B3868F072E025240CF780CC8E5839823727E5547FD1366A203F4F70FA82660B8401D4D2D06FD9A4036D14C53F6289D6FDC724E7D06F31ED93AC1B54083D9B9FCF09B135FDE9F4F6C1F0BA0142C3715E49015958C45315859DB12D942D75497FB51ED\",\"expiry\":\"251031\",\"checksum\":\"08374162F808F8DAD0CECB8FCD1AF5F64F213D6F\",\"exponent\":\"02\"},{\"rid\":\"A000000228\",\"keyId\":\"40\",\"rsaArithmeticIndex\":\"01\",\"module\":\"EBA2E3FBE75D51C519B7A498CFE53F51519B292C1BBE0C78C14FBE38E3717DE0C0ECC04605879EF617B97ED1E8E989FCD2C7DDF61EE09E96F7EADA7D9F553426D6D6A8BE4DCF943D6C8F3627265520F757BBA16FF68749F3D796A0AAACA0ED0929BE112BF7CAB87BED4D9B5DD9B7A0CAC7F9CA513A6BFC03B4C20EFDC03E2B58D76E2ABF466665CB9D64AA412FBBF85259C480DA2F0896AB28FBB26022EFAE74CCDB9C36749E8D29AE4069A1298A0B07A7F72DFF8E6F442A2393DFF7E4E1F06F\",\"expiry\":\"301231\",\"checksum\":\"YD535A86C7A766CDAA07EA8766C7A2B688B7F4F0\",\"exponent\":\"03\"},{\"rid\":\"A000000228\",\"keyId\":\"55\",\"rsaArithmeticIndex\":\"01\",\"module\":\"EBA2E3FBE75D51C519B7A498CFE53F51519B292C1BBE0C78C14FBE38E3717DE0C0ECC04605879EF617B97ED1E8E989FCD2C7DDF61EE09E96F7EADA7D9F553426D6D6A8BE4DCF943D6C8F3627265520F757BBA16FF68749F3D796A0AAACA0ED0929BE112BF7CAB87BED4D9B5DD9B7A0CAC7F9CA513A6BFC03B4C20EFDC03E2B58D76E2ABF466665CB9D64AA412FBBF85259C480DA2F0896AB28FBB26022EFAE74CCDB9C36749E8D29AE4069A1298A0B07A7F72DFF8E6F442A2393DFF7E4E1F06F\",\"expiry\":\"250624\",\"checksum\":\"YD535A86C7A766CDAA07EA8766C7A2B688B7F4F0\",\"exponent\":\"01\"},{\"rid\":\"A000000228\",\"keyId\":\"65\",\"rsaArithmeticIndex\":\"01\",\"module\":\"EBA2E3FBE75D51C519B7A498CFE53F51519B292C1BBE0C78C14FBE38E3717DE0C0ECC04605879EF617B97ED1E8E989FCD2C7DDF61EE09E96F7EADA7D9F553426D6D6A8BE4DCF943D6C8F3627265520F757BBA16FF68749F3D796A0AAACA0ED0929BE112BF7CAB87BED4D9B5DD9B7A0CAC7F9CA513A6BFC03B4C20EFDC03E2B58D76E2ABF466665CB9D64AA412FBBF85259C480DA2F0896AB28FBB26022EFAE74CCDB9C36749E8D29AE4069A1298A0B07A7F72DFF8E6F442A2393DFF7E4E1F06F\",\"expiry\":\"301231\",\"checksum\":\"48AE1E709DBED111111111111111111111111111\",\"exponent\":\"03\"},{\"rid\":\"A000000228\",\"keyId\":\"90\",\"rsaArithmeticIndex\":\"02\",\"module\":\"B6D73BF68564C88A1AEE8BA70A5F60CE495CA722E097DADEEBB83B28040B1BAD16DBC9AC3CD181BA89193638E600AF397D220F0339A8E792AA08C1878482ACC463B3B3A257AE8667CDBC1D6613CB9CBB612830FDA7F7BA689A148EFFF34476F6E0A70C819C10B3B6150909B58BF9403F5BB2E9790EE82C50C8D6FB267C726DC255AE97FABF5A357B2A0FBD1387168D83B25ECD912027B3868F072E025240CF780CC8E5839823727E5547FD1366A203F4F70FA82660B8401D4D2D06FD9A4036D14C53F6289D6FDC724E7D06F31ED93AC1B54083D9B9FCF09B135FDE9F4F6C1F0BA0142C3715E49015958C45315859DB12D942D75497FB51ED\",\"expiry\":\"251031\",\"checksum\":\"1234111111111111111111111111111111111111\",\"exponent\":\"02\"},{\"rid\":\"A000000065\",\"keyId\":\"11\",\"rsaArithmeticIndex\":\"01\",\"module\":\"A2583AA40746E3A63C22478F576D1EFC5FB046135A6FC739E82B55035F71B09BEB566EDB9968DD649B94B6DEDC033899884E908C27BE1CD291E5436F762553297763DAA3B890D778C0F01E3344CECDFB3BA70D7E055B8C760D0179A403D6B55F2B3B083912B183ADB7927441BED3395A199EEFE0DEBD1F5FC3264033DA856F4A8B93916885BD42F9C1F456AAB8CFA83AC574833EB5E87BB9D4C006A4B5346BD9E17E139AB6552D9C58BC041195336485\",\"expiry\":\"301231\",\"checksum\":\"D9FD62C9DD4E6DE7741E9A17FB1FF2C5DB948BCB\",\"exponent\":\"03\"},{\"rid\":\"A000000065\",\"keyId\":\"13\",\"rsaArithmeticIndex\":\"01\",\"module\":\"A3270868367E6E29349FC2743EE545AC53BD3029782488997650108524FD051E3B6EACA6A9A6C1441D28889A5F46413C8F62F3645AAEB30A1521EEF41FD4F3445BFA1AB29F9AC1A74D9A16B93293296CB09162B149BAC22F88AD8F322D684D6B49A12413FC1B6AC70EDEDB18EC1585519A89B50B3D03E14063C2CA58B7C2BA7FB22799A33BCDE6AFCBEB4A7D64911D08D18C47F9BD14A9FAD8805A15DE5A38945A97919B7AB88EFA11A88C0CD92C6EE7DC352AB0746ABF13585913C8A4E04464B77909C6BD94341A8976C4769EA6C0D30A60F4EE8FA19E767B170DF4FA80312DBA61DB645D5D1560873E2674E1F620083F30180BD96CA589\",\"expiry\":\"250331\",\"checksum\":\"54CFAE617150DFA09D3F901C9123524523EBEDF3\",\"exponent\":\"03\"},{\"rid\":\"A000000152\",\"keyId\":\"5D\",\"rsaArithmeticIndex\":\"01\",\"module\":\"AD938EA9888E5155F8CD272749172B3A8C504C17460EFA0BED7CBC5FD32C4A80FD810312281B5A35562800CDC325358A9639C501A537B7AE43DF263E6D232B811ACDB6DDE979D55D6C911173483993A423A0A5B1E1A70237885A241B8EEBB5571E2D32B41F9CC5514DF83F0D69270E109AF1422F985A52CCE04F3DF269B795155A68AD2D6B660DDCD759F0A5DA7B64104D22C2771ECE7A5FFD40C774E441379D1132FAF04CDF55B9504C6DCE9F61776D81C7C45F19B9EFB3749AC7D486A5AD2E781FA9D082FB2677665B99FA5F1553135A1FD2A2A9FBF625CA84A7D736521431178F13100A2516F9A43CE095B032B886C7A6AB126E203BE7\",\"expiry\":\"231231\",\"checksum\":\"B51EC5F7DE9BB6D8BCE8FB5F69BA57A04221F39B\",\"exponent\":\"03\"},{\"rid\":\"A000000152\",\"keyId\":\"5C\",\"rsaArithmeticIndex\":\"01\",\"module\":\"833F275FCF5CA4CB6F1BF880E54DCFEB721A316692CAFEB28B698CAECAFA2B2D2AD8517B1EFB59DDEFC39F9C3B33DDEE40E7A63C03E90A4DD261BC0F28B42EA6E7A1F307178E2D63FA1649155C3A5F926B4C7D7C258BCA98EF90C7F4117C205E8E32C45D10E3D494059D2F2933891B979CE4A831B301B0550CDAE9B67064B31D8B481B85A5B046BE8FFA7BDB58DC0D7032525297F26FF619AF7F15BCEC0C92BCDCBC4FB207D115AA65CD04C1CF982191\",\"expiry\":\"231231\",\"checksum\":\"60154098CBBA350F5F486CA31083D1FC474E31F8\",\"exponent\":\"03\"},{\"rid\":\"A000000152\",\"keyId\":\"5B\",\"rsaArithmeticIndex\":\"01\",\"module\":\"D3F45D065D4D900F68B2129AFA38F549AB9AE4619E5545814E468F382049A0B9776620DA60D62537F0705A2C926DBEAD4CA7CB43F0F0DD809584E9F7EFBDA3778747BC9E25C5606526FAB5E491646D4DD28278691C25956C8FED5E452F2442E25EDC6B0C1AA4B2E9EC4AD9B25A1B836295B823EDDC5EB6E1E0A3F41B28DB8C3B7E3E9B5979CD7E079EF024095A1D19DD\",\"expiry\":\"231231\",\"checksum\":\"4DC5C6CAB6AE96974D9DC8B2435E21F526BC7A60\",\"exponent\":\"03\"},{\"rid\":\"A000000025\",\"keyId\":\"68\",\"rsaArithmeticIndex\":\"01\",\"module\":\"F4D198F2F0CF140E4D2D81B765EB4E24CED4C0834822769854D0E97E8066CBE465029B3F410E350F6296381A253BE71A4BBABBD516625DAE67D073D00113AAB9EA4DCECA29F3BB7A5D46C0D8B983E2482C2AD759735A5AB9AAAEFB31D3E718B8CA66C019ECA0A8BE312E243EB47A62300620BD51CF169A9194C17A42E51B34D83775A98E80B2D66F4F98084A448FE0507EA27C905AEE72B62A8A29438B6A4480FFF72F93280432A55FDD648AD93D82B9ECF01275C0914BAD8EB3AAF46B129F8749FEA425A2DCDD7E813A08FC0CA7841EDD49985CD8BC6D5D56F17AB9C67CEC50BA422440563ECCE21699E435C8682B6266393672C693D8B7\",\"expiry\":\"301231\",\"checksum\":\"415E5FE9EC966C835FBB3E6F766A9B1A4B8674C3\",\"exponent\":\"03\"},{\"rid\":\"A000000025\",\"keyId\":\"C9\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B362DB5733C15B8797B8ECEE55CB1A371F760E0BEDD3715BB270424FD4EA26062C38C3F4AAA3732A83D36EA8E9602F6683EECC6BAFF63DD2D49014BDE4D6D603CD744206B05B4BAD0C64C63AB3976B5C8CAAF8539549F5921C0B700D5B0F83C4E7E946068BAAAB5463544DB18C63801118F2182EFCC8A1E85E53C2A7AE839A5C6A3CABE73762B70D170AB64AFC6CA482944902611FB0061E09A67ACB77E493D998A0CCF93D81A4F6C0DC6B7DF22E62DB\",\"expiry\":\"301231\",\"checksum\":\"8E8DFF443D78CD91DE88821D70C98F0638E51E49\",\"exponent\":\"03\"},{\"rid\":\"A000000025\",\"keyId\":\"C8\",\"rsaArithmeticIndex\":\"01\",\"module\":\"BF0CFCED708FB6B048E3014336EA24AA007D7967B8AA4E613D26D015C4FE7805D9DB131CED0D2A8ED504C3B5CCD48C33199E5A5BF644DA043B54DBF60276F05B1750FAB39098C7511D04BABC649482DDCF7CC42C8C435BAB8DD0EB1A620C31111D1AAAF9AF6571EEBD4CF5A08496D57E7ABDBB5180E0A42DA869AB95FB620EFF2641C3702AF3BE0B0C138EAEF202E21D\",\"expiry\":\"301231\",\"checksum\":\"33BD7A059FAB094939B90A8F35845C9DC779BD50\",\"exponent\":\"03\"},{\"rid\":\"A000000025\",\"keyId\":\"68\",\"rsaArithmeticIndex\":\"04\",\"module\":\"F4D198F2F0CF140E4D2D81B765EB4E24CED4C0834822769854D0E97E8066CBE465029B3F410E350F6296381A253BE71A4BBABBD516625DAE67D073D00113AAB9EA4DCECA29F3BB7A5D46C0D8B983E2482C2AD759735A5AB9AAAEFB31D3E718B8CA66C019ECA0A8BE312E243EB47A62300620BD51CF169A9194C17A42E51B34D83775A98E80B2D66F4F98084A448FE0507EA27C905AEE72B62A8A29438B6A4480FFF72F93280432A55FDD648AD93D82B9ECF01275C0914BAD8EB3AAF46B129F8749FEA425A2DCDD7E813A08FC0CA7841EDD49985CD8BC6D5D56F17AB9C67CEC50BA422440563ECCE21699E435C8682B6266393672C693D8B7\",\"expiry\":\"301231\",\"checksum\":\"415E5FE9EC966C835FBB3E6F766A9B1A4B8674C3\",\"exponent\":\"03\"},{\"rid\":\"A000000025\",\"keyId\":\"CA\",\"rsaArithmeticIndex\":\"01\",\"module\":\"C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3A97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E3\",\"expiry\":\"301231\",\"checksum\":\"6BDA32B1AA171444C7E8F88075A74FBFE845765F\",\"exponent\":\"03\"}],\"terminalConfig\":{\"merchantId\":\"202209354192218\",\"terminalId\":\"6383593278311080\",\"terminalCurrencyCode\":\"682\",\"merchantCategoryCode\":\"5411\",\"merchantNameAndLocation\":null,\"terminalCountryCode\":\"682\"},\"merchantDetails\":{\"merchantNameEN\":\"ALSAHEI BRKAT SUPERMARKET1\",\"merchantNameAR\":\"اختبار تيست تيست تيست تيست تيست تيست\",\"merchantNameAddress1EN\":\"Abdullah Bin JAfar\",\"merchantNameAddress2EN\":\"QURAISH STREET\",\"merchantNameAddress1AR\":\"محلات العماري\",\"merchantNameAddress2AR\":\"شارع قابل\",\"merchantNameCityEN\":\"Riyadh\",\"merchantNameCityAR\":\"الدمام\",\"merchantPostalCode\":\"0000021473\"}}}";    }
    @Override
    public void evaluate(GetParametersRequest request) throws CommonValidationException {
        deviceProfileValidator.checkDeviceProfileFlagsForTMS(request.getDeviceMetadata().getDeviceId(),request.getApiInContext().getInputRefId());
    }

}
