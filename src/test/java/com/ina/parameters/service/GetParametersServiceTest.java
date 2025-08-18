package com.ina.parameters.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ina.CommonObjects;
import com.ina.common.config.AppContext;
import com.ina.common.crypto.service.DataDecryptionService;
import com.ina.common.crypto.service.DataEncryptionService;
import com.ina.common.model.ApiInContext;
import com.ina.common.model.DeviceMetadata;
import com.ina.common.model.SecureReqMetadata;
import com.ina.common.model.SecureRespMetadata;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.utils.CommonUtils;
import com.ina.common.utils.HashUtils;
import com.ina.common.validator.DeviceProfileValidator;
import com.ina.config.RequestPropertyConfig;
import com.ina.dao.EMVParametersRepository;
import com.ina.dao.entity.EMVParameters;
import com.ina.parameters.model.*;
import com.ina.parameters.utils.*;
import com.ina.tms.packages.xml.v8.catm217.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetParametersServiceTest extends CommonObjects {

    @Mock
    private EMVParametersRepository emvParametersRepository;


    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DataEncryptionService dataEncryptionService;

    @Mock
    private DataDecryptionService dataDecryptionService;
    @Mock
    RequestPropertyConfig propertyConfig;
    @Mock
    SecUtils secUtils;
    @Mock
    private CommonUtils commonUtils;

    @Mock
    private InaPayMessages inaPayMessages;

    @Mock
    Marshal marshal;

    @Mock
    HttpClient httpClient;
    @Mock
    HashUtils hashUtils;

    @Mock
    private AppContext appContext;

    @Mock
    private DeviceProfileValidator deviceProfileValidator;



    @Spy
    @InjectMocks
    private GetParametersService getParametersService;



    @Test
    public void testGetParametersSuccess() throws Exception {

        String decryptedJson = getRequestData();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        GetParametersRequestData mockRequestData = mapper.readValue(decryptedJson, GetParametersRequestData.class);
        GetParametersRequest mockRequest = getMockRequest();

        SecureRespMetadata secureRespMetadata = new SecureRespMetadata();
        secureRespMetadata.setData("data");
        secureRespMetadata.setSalt("salt");
        secureRespMetadata.setSignature("sign");

        JsonNode root = mapper.readTree(getData());
        JsonNode emvParametersNode = root.get("emvParameters");
        String emvParamsJson = mapper.writeValueAsString(emvParametersNode);
        TmsParams params = mapper.readValue(emvParamsJson, TmsParams.class);

        String xmlResponse = readXmlAsString();
        Object respObject = maptoParamDocObject(xmlResponse);

        JsonMapperUtil.Result mockResult = new JsonMapperUtil.Result(
                params.getAids(), params.getCpks(), params.getTerminalConfig(), params.getMerchantDetails()
        );


        try (MockedStatic<JsonMapperUtil> mockedStatic = mockStatic(JsonMapperUtil.class)) {
            mockedStatic.when(() -> JsonMapperUtil.getResult(any(), any()))
                    .thenReturn(mockResult);


            when(dataDecryptionService.decryptData(any(), any(), anyString(), anyString()))
                    .thenReturn(decryptedJson);
            when(objectMapper.readValue(anyString(), eq(GetParametersRequestData.class)))
                    .thenReturn(mockRequestData);

            EMVParameters mockEmvParameters = getEmvParameters();
            when(emvParametersRepository.findByTrsMidAndTerminalIdAndDeviceId(anyString(), anyString(), anyString()))
                    .thenReturn(mockEmvParameters);
            when(emvParametersRepository.save(any(EMVParameters.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(propertyConfig.getKrdSign2()).thenReturn("test.model");
            when(propertyConfig.getShortName()).thenReturn("GeideaUAT01");
            when(marshal.marshalToXml(any(com.ina.tms.packages.xml.v8.catm118.Document.class), eq("Document")))
                    .thenReturn(getReqXmlData());
            when(dataEncryptionService.encryptData(any(), any(), any(), any()))
                    .thenReturn(secureRespMetadata);
            when(httpClient.exchange(any(), any()))
                    .thenReturn(xmlResponse);
            when(marshal.getExpectedNsUri()).thenReturn("urn:iso:std:iso:20022:tech:xsd:catm.003.001.08");
            when(marshal.parseXml(anyString()))
                    .thenReturn(respObject);

            try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
                commonUtilsMockedStatic.when(() ->
                        CommonUtils.getApiOutContext(
                                anyString(), anyString(), any(InaPayMessages.class), anyString())
                ).thenReturn(buildApiOutContextData());
                ParameterSecureResponse response = getParametersService.getParameters(mockRequest);

                assertNotNull(response);
                assertNotNull(response.getSecureRespMetadata());

            }
        }
    }


    @Test
    public void testGetParametersSuccessWithNewDevice() throws Exception {
        String decryptedJson = getRequestData();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        GetParametersRequestData mockRequestData = mapper.readValue(decryptedJson, GetParametersRequestData.class);
        GetParametersRequest mockRequest = getMockRequest();

        SecureRespMetadata secureRespMetadata = new SecureRespMetadata();
        secureRespMetadata.setData("data");
        secureRespMetadata.setSalt("salt");
        secureRespMetadata.setSignature("sign");

        JsonNode root = mapper.readTree(getData());
        JsonNode emvParametersNode = root.get("emvParameters");
        String emvParams = mapper.writeValueAsString(emvParametersNode);
        TmsParams params = mapper.readValue(emvParams, TmsParams.class);
        String xml = readXmlAsString();
        Object respObject = maptoParamDocObject(xml);

        JsonMapperUtil.Result result = new JsonMapperUtil.Result(
                params.getAids(), params.getCpks(), params.getTerminalConfig(),params.getMerchantDetails()
        );

        try (MockedStatic<JsonMapperUtil> mockedStatic = mockStatic(JsonMapperUtil.class)) {
            mockedStatic.when(() -> JsonMapperUtil.getResult(any(), any()))
                    .thenReturn(result);

            when(dataDecryptionService.decryptData(any(), any(), anyString(), anyString()))
                    .thenReturn(decryptedJson);
            when(objectMapper.readValue(anyString(), eq(GetParametersRequestData.class)))
                    .thenReturn(mockRequestData);
            EMVParameters savedEmvParameters = new EMVParameters();
            savedEmvParameters.setParamCheckSum("dummy-checksum");
            when(emvParametersRepository.findByTrsMidAndTerminalIdAndDeviceId(anyString(), anyString(), anyString()))
                    .thenReturn(null)
                    .thenReturn(savedEmvParameters);

            when(emvParametersRepository.save(any(EMVParameters.class)))
                    .thenAnswer(invocation -> {
                        EMVParameters param = invocation.getArgument(0);
                        if (param.getParamCheckSum() == null) {
                            param.setParamCheckSum("new-checksum");
                        }
                        return param;
                    });

            when(propertyConfig.getKrdSign2()).thenReturn("test.model");
            when(propertyConfig.getShortName()).thenReturn("GeideaUAT01");
            when(marshal.marshalToXml(any(com.ina.tms.packages.xml.v8.catm118.Document.class), eq("Document")))
                    .thenReturn(getReqXmlData());
            when(dataEncryptionService.encryptData(any(), any(), any(), any()))
                    .thenReturn(secureRespMetadata);
            when(httpClient.exchange(any(), any())).thenReturn(xml);
            when(marshal.getExpectedNsUri()).thenReturn("urn:iso:std:iso:20022:tech:xsd:catm.003.001.08");
            when(marshal.parseXml(anyString())).thenReturn(respObject);
            try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
                commonUtilsMockedStatic.when(() ->
                        CommonUtils.getApiOutContext(
                                anyString(), anyString(), any(InaPayMessages.class), anyString())
                ).thenReturn(buildApiOutContextData());
                ParameterSecureResponse response = getParametersService.getParameters(mockRequest);
                assertNotNull(response);
                assertNotNull(response.getSecureRespMetadata());

            }
        }
    }


    private static GetParametersRequest getMockRequest() {
        ApiInContext apiInContext = getApiInContext();
        DeviceMetadata deviceMetadata = getDeviceMetadata();
        SecureReqMetadata secureReqMetadata = new SecureReqMetadata();
        secureReqMetadata.setSalt("salt");
        secureReqMetadata.setData("data");
        secureReqMetadata.setSignature("sign");
        GetParametersRequest request = new GetParametersRequest();
        request.setApiInContext(apiInContext);
        request.setDeviceMetadata(deviceMetadata);
        request.setSecureReqMetadata(secureReqMetadata);
        return request;
    }

    private static DeviceMetadata getDeviceMetadata() {
        DeviceMetadata deviceMetadata = new DeviceMetadata();
        deviceMetadata.setDeviceId("DEVICE345");
        return deviceMetadata;
    }

    @Test
    public void testRegisterParameterAckNewParametersRegistrationSuccess() throws Exception {
        String decryptedJson = getRequestData();
        ObjectMapper mapper = new ObjectMapper();
        GetParametersRequestData mockRequestData = mapper.readValue(decryptedJson, GetParametersRequestData.class);

        com.ina.tms.packages.xml.v8.catm217.Document document = new com.ina.tms.packages.xml.v8.catm217.Document();

        ManagementPlanReplacementV07 mgmtPlanReplacement = new ManagementPlanReplacementV07();
        ManagementPlan7 mgmtPlan = new ManagementPlan7();
        TerminalManagementDataSet24 dataSet = new TerminalManagementDataSet24();
        ManagementPlanContent7 content = new ManagementPlanContent7();
        TMSAction7 action = new TMSAction7();
        DataSetIdentification7 dataSetId = new DataSetIdentification7();

        dataSetId.setNm("Parameters");
        dataSetId.setTp(DataSetCategory12Code.PARA);

        action.setDataSetId(dataSetId);
        content.getActn().add(action);
        dataSet.setCntt(content);
        mgmtPlan.setDataSet(dataSet);
        mgmtPlanReplacement.setMgmtPlan(mgmtPlan);
        document.setMgmtPlanRplcmnt(mgmtPlanReplacement);

        when(propertyConfig.getKrdSign2()).thenReturn("test.model");
        when(propertyConfig.getShortName()).thenReturn("GeideaUAT01");
        when(marshal.marshalToXml(any(com.ina.tms.packages.xml.v8.catm118.Document.class), eq("Document")))
                .thenReturn(getReqXmlData());
        when(httpClient.exchange(any(), any())).thenReturn(readXmlAsString());
        when(marshal.getExpectedNsUri()).thenReturn("urn:iso:std:iso:20022:tech:xsd:catm.002.001.07");
        when(marshal.parseXml(anyString())).thenReturn(document);

        boolean response = getParametersService.registration(mockRequestData);

        assertTrue(response);
    }
    private EMVParameters getEmvParameters() {
        return EMVParameters.builder()
                .cpks("cpks")
                .merchantId("mid")
                .aids("aids")
                .id(1L)
                .terminalConfig("terminalConfig")
                .build();
    }

    private static String getData() {
        return "{\"apiOutContext\":{\"timeStamp\":\"2025-04-21T20:56:03.701379940\",\"outputRefId\":\"1262edce-3f75-4ba6-87b2-d6468f2147e4\",\"code\":\"0000\",\"message\":\"Success\"},\"emvParameters\":{\"aids\":[{\"aidDataList\":[{\"aid\":\"A000000333010101\",\"applicationName\":\"UNIONPAY\",\"securityCapability\":null,\"terminalCapability\":\"E0F0C8\",\"addTerminalCapability\":\"F000F0A001\",\"tacDenial\":\"0010000000\",\"tacOnline\":\"D84004F800\",\"tacDefault\":\"D84000A800\",\"cvmLimit\":\"000000030000\",\"floorLimit\":\"000000007500\",\"transactionLimit\":\"000000030000\",\"emvTerminalType\":\"22\",\"ttq\":null,\"limitOnDevice\":null,\"limitNoOnDevice\":null,\"posEntryMode\":null,\"kernelId\":null,\"cvmSupported\":null,\"terminalRiskMgmnt\":null}]}],\"cpks\":[{\"ridDataList\":[{\"rid\":\"A000000333\",\"keyId\":\"0B\",\"rsaArithmeticIndex\":\"01\",\"module\":\"CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157\",\"expiry\":\"301231\",\"checksum\":\"BD331F9996A490B33C13441066A09AD3FEB5F66C\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"0A\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B2AB1B6E9AC55A75ADFD5BBC34490E53C4C3381F34E60E7FAC21CC2B26DD34462B64A6FAE2495ED1DD383B8138BEA100FF9B7A111817E7B9869A9742B19E5C9DAC56F8B8827F11B05A08ECCF9E8D5E85B0F7CFA644EFF3E9B796688F38E006DEB21E101C01028903A06023AC5AAB8635F8E307A53AC742BDCE6A283F585F48EF\",\"expiry\":\"301231\",\"checksum\":\"C88BE6B2417C4F941C9371EA35A377158767E4E3\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"08\",\"rsaArithmeticIndex\":\"01\",\"module\":\"B61645EDFD5498FB246444037A0FA18C0F101EBD8EFA54573CE6E6A7FBF63ED21D66340852B0211CF5EEF6A1CD989F66AF21A8EB19DBD8DBC3706D135363A0D683D046304F5A836BC1BC632821AFE7A2F75DA3C50AC74C545A754562204137169663CFCC0B06E67E2109EBA41BC67FF20CC8AC80D7B6EE1A95465B3B2657533EA56D92D539E5064360EA4850FED2D1BF\",\"expiry\":\"301231\",\"checksum\":\"EE23B616C95C02652AD18860E48787C079E8E85A\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"09\",\"rsaArithmeticIndex\":\"01\",\"module\":\"EB374DFC5A96B71D2863875EDA2EAFB96B1B439D3ECE0B1826A2672EEEFA7990286776F8BD989A15141A75C384DFC14FEF9243AAB32707659BE9E4797A247C2F0B6D99372F384AF62FE23BC54BCDC57A9ACD1D5585C303F201EF4E8B806AFB809DB1A3DB1CD112AC884F164A67B99C7D6E5A8A6DF1D3CAE6D7ED3D5BE725B2DE4ADE23FA679BF4EB15A93D8A6E29C7FFA1A70DE2E54F593D908A3BF9EBBD760BBFDC8DB8B54497E6C5BE0E4A4DAC29E5\",\"expiry\":\"301231\",\"checksum\":\"A075306EAB0045BAF72CDD33B3B678779DE1F527\",\"exponent\":\"03\"},{\"rid\":\"A000000333\",\"keyId\":\"BF\",\"rsaArithmeticIndex\":\"01\",\"module\":\"C23ECBD7119F479C2EE546C123A585D697A7D10B55C2D28BEF0D299C01DC65420A03FE5227ECDECB8025FBC86EEBC1935298C1753AB849936749719591758C315FA150400789BB14FADD6EAE2AD617DA38163199D1BAD5D3F8F6A7A20AEF420ADFE2404D30B219359C6A4952565CCCA6F11EC5BE564B49B0EA5BF5B3DC8C5C6401208D0029C3957A8C5922CBDE39D3A564C6DEBB6BD2AEF91FC27BB3D3892BEB9646DCE2E1EF8581EFFA712158AAEC541C0BBB4B3E279D7DA54E45A0ACC3570E712C9F7CDF985CFAFD382AE13A3B214A9E8E1E71AB1EA707895112ABC3A97D0FCB0AE2EE5C85492B6CFD54885CDD6337E895CC70FB3255E3\",\"expiry\":\"301231\",\"checksum\":\"6BDA32B1AA171444C7E8F88075A74FBFE845765F\",\"exponent\":\"03\"}]}],\"terminalConfig\":{\"merchantId\":\"202209354192218\",\"terminalId\":\"6383593278311080\",\"terminalCurrencyCode\":\"682\",\"merchantCategoryCode\":\"5411\",\"merchantNameAndLocation\":null,\"terminalCountryCode\":\"682\"}}}";
    }

    private String getRequestData() {
        return "{\"apiInContext\":{\"inputRefId\":\"6c5af666-d44b-4d63-9c5f-d9de456daee2\",\"timeStamp\":\"2025-05-22T12:13:46.963\"},\"deviceMetadata\":{\"deviceId\":\"3b4b28d8-5478-31b5-8c42-5344dd67012d\"},\"requestData\":{\"keySerialNo\":\"1122334455667788\",\"serialNo\":\"P01A224022100379\",\"tid\":\"6383593278311080\",\"trsMid\":\"63835932\"}}";
    }

    private String getReqXmlData() {
        return  "<xsi:Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:catm.001.001.08\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><StsRpt><Hdr><DwnldTrf>false</DwnldTrf><FrmtVrsn>8.0</FrmtVrsn><XchgId>250629225600</XchgId><CreDtTm>2025-06-29T22:56:01.3244646</CreDtTm><InitgPty><Id>6383593278311080</Id><Tp>OriginatingPOI</Tp><Issr>MasterTerminalManager</Issr><ShrtNm>GeideaUAT01</ShrtNm></InitgPty><RcptPty><Id>SPTMS2.0</Id><Tp>MasterTerminalManager</Tp></RcptPty></Hdr><StsRpt><POIId><Id>6383593278311080</Id><Tp>OriginatingPOI</Tp><Issr>MasterTerminalManager</Issr></POIId><DataSet><Id><Tp>StatusReport</Tp><CreDtTm>2025-06-29T22:56:06.2462184</CreDtTm></Id><Cntt><POIDtTm>2025-06-29T22:56:06.2493178</POIDtTm><DataSetReqrd><Id><Tp>Parameters</Tp></Id><SsnKey><AddtlMgmtInf><Nm>appVersion</Nm><Val>V10.0</Val></AddtlMgmtInf><AddtlMgmtInf><Nm>keySerialNo</Nm><Val>1122334455667788</Val></AddtlMgmtInf><AddtlMgmtInf><Nm>model</Nm><Val>P01</Val></AddtlMgmtInf><AddtlMgmtInf><Nm>trsmIdCounter</Nm><Val>000001</Val></AddtlMgmtInf><AddtlMgmtInf><Nm>make</Nm><Val>Geidea</Val></AddtlMgmtInf><AddtlMgmtInf><Nm>trsmId</Nm><Val>63835932</Val></AddtlMgmtInf><AddtlMgmtInf><Nm>serialNo</Nm><Val>P01A224022100379</Val></AddtlMgmtInf></SsnKey></DataSetReqrd></Cntt></DataSet></StsRpt><SctyTrlr><CnttTp>SIGN</CnttTp><SgndData><DgstAlgo><Algo>HS25</Algo></DgstAlgo><NcpsltdCntt><CnttTp>DATA</CnttTp></NcpsltdCntt><Sgnr><DgstAlgo><Algo>HS25</Algo></DgstAlgo><SgntrAlgo><Algo>ERS2</Algo></SgntrAlgo><Sgntr>oad/HFDjV22CUEehGelbh5l7msWnRjs/jMpW7YxUsuWHOCNG63mADddvFoiyCGf2YOHChu+5rYi58s7ot5/mN0YgSJX2LSgjHD57xLOvk+A+EsLDTMD/mqvts6kO95Js80P0E7K47lruiA8VLUIpiSd8UWvUMpzL3WzFRnkYmuTfvvnVfXXwQXEB2D0sCTZNyfRMg9Fkji+6g6gIGrVbbGeLpfD/XaDVWfWDctBwjGQROXyjebkKB7ZAw0fXMd3dG8DmtoDb7JH+409FFNhT6xlHUxjECmuc7uqOY96Pf5dPWeiV86WhrFHjUnSiKhcqPvP9jn28lcycGDegpuaxwQ==</Sgntr></Sgnr></SgndData></SctyTrlr></StsRpt></xsi:Document>";
    }

}
