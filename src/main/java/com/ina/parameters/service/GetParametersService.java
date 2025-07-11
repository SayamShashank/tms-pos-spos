package com.ina.parameters.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ina.common.crypto.service.DataDecryptionService;
import com.ina.common.crypto.service.DataEncryptionService;
import com.ina.common.model.ApiOutContext;
import com.ina.common.model.SecureRespMetadata;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.utils.CommonUtils;
import com.ina.common.utils.HashUtils;
import com.ina.config.RequestPropertyConfig;
import com.ina.constants.AppErrorConstants;
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

import static com.ina.constants.AppConstants.TMS;
import static com.ina.parameters.utils.JsonMapperUtil.getResult;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Slf4j
public class GetParametersService {

    private final InaPayMessages inaPayMessages;
    private final EMVParametersRepository emvParametersRepository;
    private final ObjectMapper objectMapper;
    private final DataEncryptionService dataEncryptionService;
    private final DataDecryptionService dataDecryptionService;
    private final CommonUtils commonUtils;
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

    public GetParametersService(InaPayMessages inaPayMessages, EMVParametersRepository emvParametersRepository, ObjectMapper objectMapper, DataEncryptionService dataEncryptionService, DataDecryptionService dataDecryptionService, CommonUtils commonUtils, RequestPropertyConfig requestPropertyConfig, HashUtils hashUtils, HttpClient httpClient, Marshal marshal) {
        this.inaPayMessages = inaPayMessages;
        this.emvParametersRepository = emvParametersRepository;
        this.objectMapper = objectMapper;
        this.dataEncryptionService = dataEncryptionService;
        this.dataDecryptionService = dataDecryptionService;
        this.commonUtils = commonUtils;
        this.requestPropertyConfig = requestPropertyConfig;
        this.hashUtils = hashUtils;
        this.httpClient = httpClient;
        this.marshal = marshal;
    }


    public ParameterSecureResponse getParameters(GetParametersRequest request) throws JsonProcessingException {
        String inputRefId = request.getApiInContext().getInputRefId();
//        String decryptedData = request.getSecureReqMetadata().getData();
        String decryptedData = dataDecryptionService.decryptData(request.getSecureReqMetadata(), TMS,
                request.getDeviceMetadata().getDeviceId(), inputRefId);
        GetParametersRequestData requestData = objectMapper.readValue(decryptedData, GetParametersRequestData.class);
        commonUtils.validateDecryptedRequest(
                requestData.getApiInContext().getInputRefId(),
                requestData.getDeviceMetadata().getDeviceId(),
                request.getApiInContext(), request.getDeviceMetadata());
        EMVParameters savedEmvParams = getEmvParameters(requestData);
        TmsParams emvParameters = registerParameterAck(requestData,savedEmvParams);
        return getParameterSecureResponse(emvParameters, request, requestData);
    }

    @NotNull
    private ParameterSecureResponse getParameterSecureResponse(TmsParams emvParameters, GetParametersRequest request,GetParametersRequestData requestData) throws JsonProcessingException {
        ParameterSecureResponse response = new ParameterSecureResponse();
        ApiOutContext apiOutContext = CommonUtils.getApiOutContext(request.getApiInContext().getInputRefId(), AppErrorConstants.SUCCESS_CODE, inaPayMessages, inaPayMessages.get(AppErrorConstants.SUCCESS_CODE), TMS);
        ParameterResponse parameterResponse = new ParameterResponse();
        parameterResponse.setApiOutContext(apiOutContext);
        log.info("Retrieved EMV parameters:{}",emvParameters);
        if (nonNull(emvParameters) && CollectionUtils.isNotEmpty(emvParameters.getAids())) {
            parameterResponse.setEmvParameters(emvParameters);
        }else {
            log.info("EMV parameters is null and executing with static parameters:{}",getData());
            JsonNode root = objectMapper.readTree(getData());
            log.info("root parameters");
            JsonNode emvParametersNode = root.get("secureParams");
            String emvParams = objectMapper.writeValueAsString(emvParametersNode);
            TmsParams params = objectMapper.readValue(emvParams, TmsParams.class);
            log.info("tms parameters");
            parameterResponse.setEmvParameters(params);
            log.info("param response:{}",parameterResponse);
        }
        String data = objectMapper.writeValueAsString(parameterResponse);
        String checksum = hashUtils.generateSHA512(data);
        EMVParameters checksumParams=getEmvParameters(requestData);
        checksumParams.setParamCheckSum(checksum);
        EMVParameters parameters = emvParametersRepository.save(checksumParams);
        log.info("paramCheckSum:{}",parameters.getParamCheckSum());
//        SecureRespMetadata secureRespMetadata = new SecureRespMetadata();
//        secureRespMetadata.setData(data);
        SecureRespMetadata secureRespMetadata = getSecureRespMetadata(request, data);
        response.setSecureRespMetadata(secureRespMetadata);
        response.setApiOutContext(apiOutContext);
        return response;
    }

        private  SecureRespMetadata getSecureRespMetadata(GetParametersRequest request,String data) {
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
            log.info("respXmlData:{}",resXmlData);
            log.info("resp End");
            Object xmlRespObj = marshal.parseXml(respXmlData);
            String value = objectMapper.writeValueAsString(xmlRespObj);
            log.info("xmlRespObj:{}",value);

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
            }
            else if (expectedNsUri.equals(REJECT_NS_URI)) {
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

    public ParameterResult paramDownload(GetParametersRequestData request,EMVParameters savedEmvParams) throws JsonProcessingException {

        ParameterDownload paramDld = getParameterDownload();
        Document doc = paramDld.genStsRpt(request.getRequestData());

        log.info("param dld param: ");
        String expectedNsUri = exchange(doc);
        TmsParams tmsParams = new TmsParams();
        if (expectedNsUri != null) {
            if (expectedNsUri.equals(ACCEPT_CONFIG_UPDATE_NS_URI)) {
                log.info("param success");
                EMVParameters params = getParams(request, tmsParams, savedEmvParams);
                return new ParameterResult(true,tmsParams,params);
            }
            return new ParameterResult(true,tmsParams,savedEmvParams);
        }
        return new ParameterResult(true,tmsParams,savedEmvParams);
    }

    private EMVParameters getParams(GetParametersRequestData request, TmsParams tmsParams,EMVParameters savedEmvParams) throws JsonProcessingException {
        com.ina.tms.packages.xml.v8.catm318.Document acptrConfigUpdateDoc = (com.ina.tms.packages.xml.v8.catm318.Document) this
                .getRespObj();
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
        JsonMapperUtil.Result result = getResult(appParamsList);
        tmsParams.setAids(result.aidLists());
        tmsParams.setCpks(result.ridList());
        tmsParams.setTerminalConfig(result.terminalConfig());
        String parameterDownload = result.objectMapper().writeValueAsString(tmsParams);
        log.info("Fetched Parameters:{}",parameterDownload);
        if (isNull(savedEmvParams)) {
            EMVParameters savedEmvParameters = EMVParameters.builder()
                    .deviceId(request.getDeviceMetadata().getDeviceId())
                    .merchantId(result.terminalConfig().getMerchantId())
                    .terminalId(result.terminalConfig().getTerminalId())
                    .trsMid(request.getRequestData().getTrsMid())
                    .aids(result.objectMapper().writeValueAsString(result.aidLists()))
                    .cpks(result.objectMapper().writeValueAsString(result.ridList()))
                    .terminalConfig(result.objectMapper().writeValueAsString(result.terminalConfig()))
                    .createdDate(Timestamp.valueOf(LocalDateTime.now()))
                    .updatedDate(Timestamp.valueOf(LocalDateTime.now()))
                    .build();
           return emvParametersRepository.save(savedEmvParameters);
        }else {
            savedEmvParams.setAids(result.objectMapper().writeValueAsString(result.aidLists()));
            savedEmvParams.setCpks(result.objectMapper().writeValueAsString(result.ridList()));
            savedEmvParams.setTerminalConfig(result.objectMapper().writeValueAsString(result.terminalConfig()));
            savedEmvParams.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
            return emvParametersRepository.save(savedEmvParams);
        }

    }

    @NotNull
    protected ParameterDownload getParameterDownload() {
        return new ParameterDownload(requestPropertyConfig);
    }

    private EMVParameters getEmvParameters(GetParametersRequestData request) {
        return emvParametersRepository.findByTrsMidAndTerminalIdAndDeviceId(request.getRequestData().getTrsMid(),request.getRequestData().getTid(),request.getDeviceMetadata().getDeviceId());
    }




    public record ParameterResult(boolean isPerformed, TmsParams parameters,EMVParameters emvParameters){}


    public TmsParams registerParameterAck(GetParametersRequestData request,EMVParameters savedEmvParams) throws JsonProcessingException {
        if (isNull(savedEmvParams)) {
            if (Boolean.TRUE.equals(registration(request))) {
                ParameterResult parameterResult = extractParameters(request,null);
                if (nonNull(parameterResult)  && parameterResult.isPerformed) {
                    acknowledgment(request);
                    return parameterResult.parameters;
                }
            }
        } else {
            ParameterResult parameterResult = extractParameters(request,savedEmvParams);
            if (nonNull(parameterResult)  && parameterResult.isPerformed)  {
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
        return "{\"apiOutContext\":{\"timeStamp\":\"2025-04-21T20:56:03.701379940\",\"outputRefId\":\"1262edce-3f75-4ba6-87b2-d6468f2147e4\",\"code\":\"0000\",\"message\":\"Success\"},\"emvParameters\":{\"aids\":[{\"aidDataList\":[{\"aid\":\"A000000333010101\",\"applicationName\":\"UNIONPAY\",\"securityCapability\":null,\"terminalCapability\":\"E0F0C8\",\"addTerminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"D84004F800\",\"tacDefault\":\"D84000A800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null}]}],\"cpks\":[{\"ridDataList\":[{\"rid\":\"A000000333\",\"keyId\":\"0B\",\"rsaArithmeticIndex\":\"01\",\"module\":\"CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157\",\"expiry\":\"301231\",\"checksum\":\"BD331F9996A490B33C13441066A09AD3FEB5F66C\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"0A\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EF\",\"expiry\":\"301231\",\"checksum\":\"C88BE6B2417C4F941C9371EA35A377158767E4E3\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"08\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BF\",\"expiry\":\"301231\",\"checksum\":\"EE23B616C95C02652AD18860E48787C079E8E85A\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"09\",\"rsaArithmeticIndex\":\"01\",\"module\":\"EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5\",\"expiry\":\"301231\",\"checksum\":\"A075306EAB0045BAF72CDD33B3B678779DE1F527\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"BF\",\"rsaArithmeticIndex\":\"01\",\"module\":\"C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3A97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E3\",\"expiry\":\"301231\",\"checksum\":\"6BDA32B1AA171444C7E8F88075A74FBFE845765F\",\"exponent\":\"03\"}]}],\"terminalConfig\":{\"merchantId\":\"202209354192218\",\"terminalId\":\"6383593278311080\",\"terminalCurrencyCode\":\"682\",\"merchantCategoryCode\":\"5411\",\"merchantNameAndLocation\":null,\"terminalCountryCode\":\"682\"}}}";
    }

}
