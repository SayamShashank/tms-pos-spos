package com.ina.parameters.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ina.common.crypto.service.DataDecryptionService;
import com.ina.common.crypto.service.DataEncryptionService;
import com.ina.common.model.ApiOutContext;
import com.ina.common.model.CommonRequest;
import com.ina.common.model.SecureRespMetadata;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.utils.CommonUtils;
import com.ina.config.RequestPropertyConfig;
import com.ina.dao.EMVParametersRepository;
import com.ina.dao.entity.EMVParameters;
import com.ina.parameters.messages.Acknowledgement;
import com.ina.parameters.messages.ParameterDownload;
import com.ina.parameters.messages.Registration;
import com.ina.parameters.model.*;
import com.ina.parameters.packages.xml.v8.catm118.Document;
import com.ina.parameters.packages.xml.v8.catm217.DataSetCategory12Code;
import com.ina.parameters.utils.HttpClient;
import com.ina.parameters.utils.JsonMapperUtil;
import com.ina.parameters.utils.Marshal;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.ina.constants.AppConstants.TMS;
import static com.ina.constants.AppErrorConstants.SUCCESS_CODE;
import static com.ina.parameters.utils.JsonMapperUtil.getResult;
import static com.ina.util.TMSUtil.getApiOutContext;
import static java.util.Objects.isNull;

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
    private final String rejectNsUri = "urn:iso:std:iso:20022:tech:xsd:catm.004.001.04";
    private final String mgmtPlanNsuri = "urn:iso:std:iso:20022:tech:xsd:catm.002.001.07";
    private final String acptrConfigUpdateNsUri = "urn:iso:std:iso:20022:tech:xsd:catm.003.001.08";

    public GetParametersService(InaPayMessages inaPayMessages, EMVParametersRepository emvParametersRepository, ObjectMapper objectMapper, DataEncryptionService dataEncryptionService, DataDecryptionService dataDecryptionService, CommonUtils commonUtils, RequestPropertyConfig requestPropertyConfig) {
        this.inaPayMessages = inaPayMessages;
        this.emvParametersRepository = emvParametersRepository;
        this.objectMapper = objectMapper;
        this.dataEncryptionService = dataEncryptionService;
        this.dataDecryptionService = dataDecryptionService;
        this.commonUtils = commonUtils;
        this.requestPropertyConfig = requestPropertyConfig;
    }


    public ParameterSecureResponse getParameters(GetParametersRequest request) throws JsonProcessingException {
        String inputRefId=request.getApiInContext().getInputRefId();
        String decryptedData = dataDecryptionService.decryptData(request.getSecureReqMetadata(), TMS,
                request.getDeviceMetadata().getDeviceId(), inputRefId);
        GetParametersRequestData requestData=objectMapper.readValue(request.getSecureReqMetadata().getData(),GetParametersRequestData.class);
//        commonUtils.validateDecryptedRequest(
//                requestData.getApiInContext().getInputRefId(),
//                requestData.getDeviceMetadata().getDeviceId(),
//                request.getApiInContext(),request.getDeviceMetadata());
//        TmsParams emvParameters= registerParameterAck(requestData);
        String data = getData();
        return getParameterSecureResponse(data, request);
    }



//    @NotNull
//    private ParameterSecureResponse getParameterSecureResponse(GetParametersRequest request, TmsParams emvParameters) throws JsonProcessingException {
//        ParameterResponse parameterResponse=new ParameterResponse();
//        ApiOutContext apiOutContext=getApiOutContext(request.getApiInContext().getInputRefId(),SUCCESS_CODE,inaPayMessages);
//        parameterResponse.setApiOutContext(apiOutContext);
//        parameterResponse.setEmvParameters(emvParameters);
//        String data = objectMapper.writeValueAsString(parameterResponse);
//        return getParameterSecureResponse(data, apiOutContext);
//    }

    @NotNull
    private  ParameterSecureResponse getParameterSecureResponse(String data, GetParametersRequest request) {
        ParameterSecureResponse response =new ParameterSecureResponse();
        ApiOutContext apiOutContext=getApiOutContext(request.getApiInContext().getInputRefId(),SUCCESS_CODE,inaPayMessages);

//        SecureRespMetadata secureRespMetadata=new SecureRespMetadata();
//        secureRespMetadata.setSalt("salt");
//        secureRespMetadata.setSignature("sign");
//        secureRespMetadata.setData(data);
        SecureRespMetadata secureRespMetadata = getSecureRespMetadata(request, data);
        response.setSecureRespMetadata(secureRespMetadata);
        response.setApiOutContext(apiOutContext);
        return response;
    }

        private  SecureRespMetadata getSecureRespMetadata(GetParametersRequest request,String data) {
        return dataEncryptionService.encryptData(data, TMS, request.getDeviceMetadata().getDeviceId(), request.getApiInContext().getInputRefId());
    }
    private <T> String exchange(T classObj) {

        // 1. marshal to xml
        Marshal marshal = new Marshal();
        marshal.setUseCustomWriter(Boolean.FALSE);
        marshal.setAddNs(Boolean.TRUE);
        String stsRptMsg = marshal.marshalToXml(classObj, classObj.getClass().getSimpleName());
        stsRptMsg = stsRptMsg.replaceAll("<xsi:Document", "<Document").replaceAll("</xsi:Document>", "</Document>");
        log.info("req: ");
        log.info(stsRptMsg);

        // 2. send and receive
        HttpClient httpClient = new HttpClient();
        String ep = "statusReport.htm";
        String respXmlData = httpClient.exchange(ep, stsRptMsg);
        if (respXmlData != null) {

            // 3. unmarshal and parse
            log.info("resp: ");

            log.info(respXmlData);
            this.setRespObj(marshal.parseXml(respXmlData));
            return marshal.getExpectedNsUri();
        }
        return null;
    }

    public Boolean testRegistration(GetParametersRequestData request) {


        Registration registration = new Registration(requestPropertyConfig);
        Document doc = registration.genStsRpt(request.getRequestData());

        log.info("registration: ");
        // simulate successful resp flow
        //respXmlData = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:catm.002.001.07\"><MgmtPlanRplcmnt><Hdr><DwnldTrf>false</DwnldTrf><FrmtVrsn>8.0</FrmtVrsn><XchgId>300516000023</XchgId><CreDtTm>2023-01-05T16:31:44</CreDtTm><InitgPty><Id>1189264177080814</Id><Tp>OriginatingPOI</Tp><Issr>MasterTerminalManager</Issr></InitgPty><RcptPty><Id>SPTMS2.0</Id><Tp>MasterTerminalManager</Tp></RcptPty></Hdr><MgmtPlan><POIId><Id>1189264177080814</Id><Tp>OriginatingPOI</Tp><Issr>MasterTerminalManager</Issr></POIId><DataSet><Id><Tp>ManagementPlan</Tp><CreDtTm>2023-01-05T16:29:52.000+03:00</CreDtTm></Id><Cntt><Actn><Tp>Download</Tp><DataSetId><Nm>Terminal Registered Successfully</Nm><Tp>Parameters</Tp></DataSetId><Trggr>DATE</Trggr><TmCond><StartTm>2023-01-05T16:29:52.000+03:00</StartTm></TmCond></Actn><Actn><Tp>Download</Tp><DataSetId><Tp>ManagementPlan</Tp></DataSetId><Trggr>DATE</Trggr><TmCond><StartTm>2023-01-05T16:31:52.000+03:00</StartTm></TmCond></Actn></Cntt></DataSet></MgmtPlan><SctyTrlr><CnttTp>SIGN</CnttTp><SgndData><DgstAlgo><Algo>HS25</Algo></DgstAlgo><NcpsltdCntt><CnttTp>DATA</CnttTp></NcpsltdCntt><Sgnr><DgstAlgo><Algo>HS25</Algo></DgstAlgo><SgntrAlgo><Algo>ERS2</Algo></SgntrAlgo><Sgntr>NGUzYmY5NjJkYWM1MGVlOGIyYmJkMDkwOWY0ZDI2MzhlYmQ5MjE1Yzg3ZjQxNTc3MzIyNGVhYjgzMWEwYjhkYzJmMGZlMjMxZGMxNzBhM2M3ZWEwOTJjZTA5MGJjYTQyOTQzZDY0NjgzZTRmYzVkMjBmYzM0ZGZiNjA2Mzk2MDUwZWQxMjAxMDU4OGM5OTg2ZGRkNmI4NTlhNDAxZGI2YmUxN2MyZjNmZjA3ZDE5ZmNkNmMzMzU4ZmU2NzgwYmZlYTA3MWExODFmY2ViMDRmMmVjYTI4M2Q1NmE0MjllN2Y0YzQwZDE2OWY1OWI1MDExMGYzMTIxYmIyMTEzMGI2MWQ4ZjkzOTAzN2E2N2RmODhkY2NmMzdiMGNjZWM0MmY4MDc5MGEyNmY0ODM5M2JkMDk1ODc3OTMyYjA2ZDczMGRiZjk0MWQ3NjZmZWNlYWRkNGY1ZGJkYzM3MWZhYjRiN2U2MjMyODY1OGFjYzIxMmU5M2QwNmQ4MDljNWQ0MDc4Mzc0ZDkzMmJhYjQ4ZTdmYmYxN2FmZGUyMGFkNjIwODk4YTJiZjBjNTM2MGNiOGZkZTIyMmJiOTIyM2Q0ZmI3NDYzN2JiMTY5MThiMjg5OWRmZWY0YTBiYWU5MWU0OGYyOWNkYmQwMjA4NjM4NDMxY2JjY2MyN2Q2Zjk2NjQ0N2U=</Sgntr></Sgnr></SgndData></SctyTrlr></MgmtPlanRplcmnt></Document>";
        String expectedNsUri = this.exchange(doc);
        if (expectedNsUri != null) {
            if (expectedNsUri.equals(mgmtPlanNsuri)) {
                com.ina.parameters.packages.xml.v8.catm217.Document mgmtPlanDoc = (com.ina.parameters.packages.xml.v8.catm217.Document) this
                        .getRespObj();
                log.info("mgmt plan replace: ");
                log.info(mgmtPlanDoc.getMgmtPlanRplcmnt().getMgmtPlan().getDataSet()
                        .getCntt().getActn().get(0).getDataSetId().getNm());
                // check for content/Action/DataSetId/Type=Parameters
                DataSetCategory12Code dsiType = mgmtPlanDoc.getMgmtPlanRplcmnt()
                        .getMgmtPlan().getDataSet().getCntt().getActn().get(0).getDataSetId().getTp();
                if (dsiType.value().equals("Parameters")) {
                    return true;
                }
            }
            else if (expectedNsUri.equals(rejectNsUri)) {
//                com.ina.parameters.packages.xml.v8.catm414.Document rejectNsUri
//                throw TMSUtil.throwValidationException(request.getApiInContext().getInputRefId(),REGISTRATION_REQUEST_FAILED,inaPayMessages);
            }
            return true;
        }
        return false;
    }

    public Boolean testAcknowledgment(GetParametersRequestData request) {

        Acknowledgement acknowledgement = new Acknowledgement(requestPropertyConfig);
        Document doc = acknowledgement.genStsRpt(request.getRequestData());

        log.info("param dld ack: ");
        String expectedNsUri = this.exchange(doc);
        if (expectedNsUri != null) {

            if (expectedNsUri.equals(mgmtPlanNsuri)) {
                com.ina.parameters.packages.xml.v8.catm217.Document mgmtPlanDoc = (com.ina.parameters.packages.xml.v8.catm217.Document) this
                        .getRespObj();
                // TODO: check for Content/Action/DataSetId=ManagementPlan &
                // Content/Action/DataSetId=SecurityParameters
            }
            return true;
        }
        return false;
    }

    public   paramResult testParamDownload(GetParametersRequestData request) throws JsonProcessingException {

        ParameterDownload paramDld = new ParameterDownload(requestPropertyConfig);
        Document doc = paramDld.genStsRpt(request.getRequestData());

        log.info("param dld param: ");
        String expectedNsUri = this.exchange(doc);
        TmsParams tmsParams = new TmsParams();
        if (expectedNsUri != null) {
            if (expectedNsUri.equals(acptrConfigUpdateNsUri)) {
                log.info("param success");
                com.ina.parameters.packages.xml.v8.catm318.Document acptrConfigUpdateDoc = (com.ina.parameters.packages.xml.v8.catm318.Document) this
                        .getRespObj();
                List<String> merchParamsBa = acptrConfigUpdateDoc.getAccptrCfgtnUpd()
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
                        .collect(Collectors.toList());
                JsonMapperUtil.Result result = getResult(appParamsList, merchParamsBa);
                tmsParams.setAids(result.aidLists());
                tmsParams.setCpks(result.ridList());
                tmsParams.setTerminalConfig(result.terminalConfig());
                EMVParameters savedEmvParams = getEmvParameters(request);
                if (isNull(savedEmvParams)) {
                    EMVParameters savedEmvParameters = EMVParameters.builder()
                            .deviceId(request.getDeviceMetadata().getDeviceId())
                            .merchantId(result.terminalConfig().getMerchantId())
                            .terminalId(result.terminalConfig().getTerminalId())
                            .trsmid(request.getRequestData().getTrsMid())
                            .aids(result.objectMapper().writeValueAsString(result.aidLists()))
                            .cpks(result.objectMapper().writeValueAsString(result.ridList()))
                            .terminalConfig(result.objectMapper().writeValueAsString(result.terminalConfig()))
                            .createdDate(Timestamp.valueOf(LocalDateTime.now()))
                            .updatedDate(Timestamp.valueOf(LocalDateTime.now()))
                            .build();
                    emvParametersRepository.save(savedEmvParameters);
                }else {
                    //TODO:will update any fields need to be update every expect below
                    savedEmvParams.setAids(result.objectMapper().writeValueAsString(result.aidLists()));
                    savedEmvParams.setCpks(result.objectMapper().writeValueAsString(result.ridList()));
                    savedEmvParams.setTerminalConfig(result.objectMapper().writeValueAsString(result.terminalConfig()));
                    savedEmvParams.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
                }
                String parameterDownload = result.objectMapper().writeValueAsString(tmsParams);
                log.info("Fetched Parameters:{}",parameterDownload);
            }
            return new paramResult(true,tmsParams);
        }
        return new paramResult(true,tmsParams);
    }

    private EMVParameters getEmvParameters(GetParametersRequestData request) {
        return emvParametersRepository.findByDeviceId(request.getDeviceMetadata().getDeviceId());
    }


    public record paramResult(boolean isPerformed,TmsParams parameters){}


    public TmsParams registerParameterAck(GetParametersRequestData request) throws JsonProcessingException {

        if (testRegistration(request) == Boolean.TRUE) {
            paramResult paramResult = testParamDownload(request);
            if (paramResult.isPerformed == Boolean.TRUE) {
                TmsParams parameters = paramResult.parameters;
                testAcknowledgment(request);
                return parameters;
            }
        }
        return null;
    }
    @NotNull
    private static String getData() {
        return "{"
                + "\"aids\":[{\"aidDataList\":[{"
                + "\"aid\":\"A000000333010101\","
                + "\"applicationName\":\"UNIONPAY\","
                + "\"securityCapability\":null,"
                + "\"terminalCapability\":\"E0F0C8\","
                + "\"tacDenial\":\"0010000000\","
                + "\"tacOnline\":\"D84004F800\","
                + "\"tacDefault\":\"D84000A800\","
                + "\"cvmLimit\":\"30000\","
                + "\"floorLimit\":\"7500\","
                + "\"transactionLimit\":\"30000\","
                + "\"emvTerminalType\":\"22\","
                + "\"ttq\":null,"
                + "\"limitOnDevice\":null,"
                + "\"limitNoOnDevice\":null,"
                + "\"posEntryMode\":null,"
                + "\"kernelId\":null,"
                + "\"cvmSupported\":null,"
                + "\"terminalRiskMgmnt\":null"
                + "}]}],"
                + "\"cpks\":[{\"ridDataList\":[{"
                + "\"rid\":\"A000000333\","
                + "\"keyId\":\"0B\","
                + "\"rsaArithmeticIndex\":\"01\","
                + "\"module\":\"CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157\","
                + "\"expiry\":\"301231\","
                + "\"checksum\":\"BD331F9996A490B33C13441066A09AD3FEB5F66C\","
                + "\"exponent\":\"03\""
                + "},{"
                + "\"rid\":\"A000000333\","
                + "\"keyId\":\"0A\","
                + "\"rsaArithmeticIndex\":\"01\","
                + "\"module\":\"B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EF\","
                + "\"expiry\":\"301231\","
                + "\"checksum\":\"C88BE6B2417C4F941C9371EA35A377158767E4E3\","
                + "\"exponent\":\"03\""
                + "},{"
                + "\"rid\":\"A000000333\","
                + "\"keyId\":\"08\","
                + "\"rsaArithmeticIndex\":\"01\","
                + "\"module\":\"B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BF\","
                + "\"expiry\":\"301231\","
                + "\"checksum\":\"EE23B616C95C02652AD18860E48787C079E8E85A\","
                + "\"exponent\":\"03\""
                + "},{"
                + "\"rid\":\"A000000333\","
                + "\"keyId\":\"BF\","
                + "\"rsaArithmeticIndex\":\"01\","
                + "\"module\":\"C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3A97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E3\","
                + "\"expiry\":\"301231\","
                + "\"checksum\":\"6BDA32B1AA171444C7E8F88075A74FBFE845765F\","
                + "\"exponent\":\"03\""
                + "}]}],"
                + "\"terminalConfig\":{"
                + "\"merchantId\":\"202209354192218\","
                + "\"terminalId\":\"6383593278311080\","
                + "\"terminalCurrencyCode\":\"682\","
                + "\"merchantCategoryCode\":\"5411\","
                + "\"merchantNameAndLocation\":null,"
                + "\"terminalCountryCode\":\"682\""
                + "}"
                + "}";
    }
    public ParameterResponse getParameters(CommonRequest request) {
        return null;
    }
}
