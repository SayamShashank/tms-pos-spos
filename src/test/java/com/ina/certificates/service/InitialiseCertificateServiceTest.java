package com.ina.certificates.service;

import com.ina.CommonObjects;
import com.ina.common.config.AppContext;
import com.ina.common.crypto.model.certs.*;
import com.ina.common.crypto.service.CertGenerationService;
import com.ina.common.crypto.service.ServerEncryptionAndSignatureCertificateService;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.*;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.utils.CommonUtils;
import com.ina.common.validator.DeviceProfileValidator;
import org.hibernate.sql.results.internal.InitializersList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;

import static com.ina.CommonObjects.createRequest;
import static com.ina.CommonObjects.getApiInContext;
import static com.ina.common.constants.AppErrorConstants.FAILED_CODE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InitialiseCertificateServiceTest extends CommonObjects {

    @Mock
    private CertGenerationService certGenerationService;

    @Mock
    private ServerEncryptionAndSignatureCertificateService serverEncryptionAndSignatureCertificateService;

    @Mock
    private InaPayMessages messages;

    @InjectMocks
    private InitialiseCertificateService initialiseCertificateService;

    private ApiInContext apiInContext;

    private InitialiseCertRequest initialiseCertRequest;

    private Request request;

    @Mock
    private AppContext appContext;

    @Mock
    private CommonUtils commonUtils;

    @Mock
    private DeviceProfileValidator deviceProfileValidator;

    @BeforeEach
    void setUp() {
        apiInContext = new ApiInContext();
        apiInContext.setInputRefId("ref123");
        initialiseCertRequest = new InitialiseCertRequest();
        initialiseCertRequest.setApiInContext(apiInContext);
        initialiseCertRequest.setCertificateType("SIGN");
        request = new Request();
        request.setApiInContext(apiInContext);
    }

    @Test
    void testGenerateServerCerts_Success() {
        when(messages.get("000")).thenReturn("Success");
        CertificateGenerationResponse certResponse = new CertificateGenerationResponse();
        certResponse.setStatus("success");

        when(certGenerationService.generateCertificate(eq("SIGN"), any()))
                .thenReturn(certResponse);

        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse response = initialiseCertificateService.generateServerCerts(initialiseCertRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testGenerateServerCerts_Failure() {
        when(messages.get("999")).thenReturn("Failed");
        CertificateGenerationResponse certResponse = new CertificateGenerationResponse();
        certResponse.setStatus("failure");
        certResponse.setMessage("Failed");
        when(certGenerationService.generateCertificate(eq("SIGN"), any()))
                .thenReturn(certResponse);

        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());

            CommonResponse response = initialiseCertificateService.generateServerCerts(initialiseCertRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testGenerateServerCerts_Exception() {
        when(certGenerationService.generateCertificate(eq("SIGN"), any()))
                .thenThrow(new CommonValidationException("ERR001", "Validation error"));

        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse response = initialiseCertificateService.generateServerCerts(initialiseCertRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testGenerateRootCertificate_Success() {
        when(messages.get("000")).thenReturn("Success");
        PublishRootCertificateRequest publishRootCertificateRequest = new PublishRootCertificateRequest();
        RootCertificateResponse rootCertificateResponse = new RootCertificateResponse();
        rootCertificateResponse.setCert("cert-data");
        rootCertificateResponse.setSkLmk("key-data");

        publishRootCertificateRequest.setRootCertificateResponse(rootCertificateResponse);
        publishRootCertificateRequest.setApiInContext(apiInContext);

        CertificateGenerationResponse certResponse = new CertificateGenerationResponse();
        certResponse.setStatus("success");

        when(certGenerationService.saveCertificatesIntoDB(any(), any(), any()))
                .thenReturn(certResponse);
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());

            CommonResponse response = initialiseCertificateService.generateRootCertificate(publishRootCertificateRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testGenerateRootCertificateFailure() {
        when(messages.get("999")).thenReturn("Failed");
        PublishRootCertificateRequest publishRootCertificateRequest = new PublishRootCertificateRequest();
        RootCertificateResponse rootCertificateResponse = new RootCertificateResponse();
        rootCertificateResponse.setCert("cert data");
        rootCertificateResponse.setSkLmk("key data");

        publishRootCertificateRequest.setRootCertificateResponse(rootCertificateResponse);
        publishRootCertificateRequest.setApiInContext(apiInContext);

        CertificateGenerationResponse certResponse = new CertificateGenerationResponse();
        certResponse.setStatus("failure");

        when(certGenerationService.saveCertificatesIntoDB(any(), any(), any()))
                .thenReturn(certResponse);
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse response = initialiseCertificateService.generateRootCertificate(publishRootCertificateRequest);
            assertNotNull(response);
        }
    }


    @Test
    void testGenerateRootCertificateException() {
        PublishRootCertificateRequest publishRootCertificateRequest = new PublishRootCertificateRequest();
        publishRootCertificateRequest.setCertificateType(" ");
        RootCertificateResponse rootResponse = new RootCertificateResponse();
        rootResponse.setCert("cert-data");
        rootResponse.setSkLmk("key-data");
        publishRootCertificateRequest.setRootCertificateResponse(rootResponse);
        publishRootCertificateRequest.setApiInContext(getApiInContext());
        when(certGenerationService.saveCertificatesIntoDB(any(), any(), any()))
                .thenThrow(new CommonValidationException("ERR001", "Validation error"));
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse response = initialiseCertificateService.generateRootCertificate(publishRootCertificateRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testGenerateL4Certs_Success() {
        when(messages.get("000")).thenReturn("Success");
        ApiOutContext outContext = new ApiOutContext();
        outContext.setStatus("Success");

        ServerCertsInfoResponse serverCertsInfoResponse = new ServerCertsInfoResponse();
        serverCertsInfoResponse.setApiOutContext(outContext);

        when(serverEncryptionAndSignatureCertificateService
                .generateServerL4Certificates(any(), any())).thenReturn(serverCertsInfoResponse);

        CommonResponse response = initialiseCertificateService.generateL4Certs(initialiseCertRequest);
        assertEquals("Success", response.getApiOutContext().getStatus());
    }

    @Test
    void testGenerateL4CertsFailure() {
        when(messages.get("000")).thenReturn("Failed");
        ApiOutContext outContext = new ApiOutContext();
        outContext.setStatus("Failure");

        ServerCertsInfoResponse serverCertsInfoResponse = new ServerCertsInfoResponse();
        serverCertsInfoResponse.setApiOutContext(outContext);

        when(serverEncryptionAndSignatureCertificateService
                .generateServerL4Certificates(any(), any())).thenReturn(serverCertsInfoResponse);
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse response = initialiseCertificateService.generateL4Certs(initialiseCertRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testGenerateL4CertsException() {
        when(serverEncryptionAndSignatureCertificateService.generateServerL4Certificates(any(), any()))
                .thenThrow(new CommonValidationException("ERR001", "Validation error"));
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse response = initialiseCertificateService.generateL4Certs(initialiseCertRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testGetAllServerCerts() {
        when(messages.get("000")).thenReturn("Success");
        ServerCertsResponse certsResponse = new ServerCertsResponse();
        when(certGenerationService.getAllServerCerts()).thenReturn(certsResponse);
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            ServerCertsResponse response = initialiseCertificateService.getAllServerCerts(request);
            assertNotNull(response);
        }
    }

    @Test
    void testViewCertInfoSuccess() {
        ViewCertificateInfo info = new ViewCertificateInfo();
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setMessage("Success");
        info.setApiOutContext(apiOutContext);
        when(certGenerationService.viewCertificate(any(), any())).thenReturn(info);
        when(messages.get("000")).thenReturn("Success");
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            ViewCertificateInfo response = initialiseCertificateService.viewCertInfo(initialiseCertRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testViewCertInfoNotAvailable() {
        when(certGenerationService.viewCertificate(anyString(), anyString()))
                .thenReturn(null);
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            ViewCertificateInfo response = initialiseCertificateService.viewCertInfo(initialiseCertRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testViewCertInfoException() {
        when(certGenerationService.viewCertificate(anyString(), anyString()))
                .thenThrow(new CommonValidationException("ERR001", "Validation error"));
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse response = initialiseCertificateService.viewCertInfo(initialiseCertRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testGetServerCertificateStatusSuccess() {
        InitialiseCertRequest certRequest = new InitialiseCertRequest();
        certRequest.setApiInContext(getApiInContext());
        certRequest.setCertificateType("SSL");
        ServerCertsStatusResponse certsStatusResponse = new ServerCertsStatusResponse();
        when(certGenerationService.getServerCertificateStatus("SSL"))
                .thenReturn(certsStatusResponse);
        when(messages.get("000")).thenReturn("Success");
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            ServerCertsStatusResponse result = initialiseCertificateService.getServerCertificateStatus(certRequest);
            assertNotNull(result);

        }
    }

    @Test
    void testGetDeviceCertsSuccess() {
        CommonRequest commonRequest = buildCommonRequest();
        DeviceCertsResponse deviceCertsResponse = buildDeviceCertsResponse();
        when(certGenerationService.getAllDeviceSpecificCerts(commonRequest.getDeviceMetadata().getDeviceId(), commonRequest.getApiInContext().getInputRefId()))
                .thenReturn(deviceCertsResponse);
        when(messages.get("000")).thenReturn("Success");
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            DeviceCertsResponse response = initialiseCertificateService.getDeviceCerts(commonRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testGetDeviceCertsFailure() {
        CommonRequest commonRequest = buildCommonRequest();
        DeviceCertsResponse deviceCertsResponse = buildEmptyDeviceCertsResponse();
        when(certGenerationService.getAllDeviceSpecificCerts(commonRequest.getDeviceMetadata().getDeviceId(), commonRequest.getApiInContext().getInputRefId()))
                .thenReturn(deviceCertsResponse);
        when(messages.get("999")).thenReturn("Failure");
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            DeviceCertsResponse response = initialiseCertificateService.getDeviceCerts(commonRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testEvaluate() {
        ApiInContext apiInContext=buildApiInContext();
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(getApiOutContextData());
            initialiseCertificateService.evaluate(apiInContext);
        }
    }

        @Test
        void testEvaluateThrowExceptionWhenTimeStampNull(){
            ApiInContext mockContext =new ApiInContext();
            mockContext.setInputRefId("input123");
            mockContext.setTimeStamp(null);
            Exception exception = assertThrows(Exception.class, () -> {
                initialiseCertificateService.evaluate(mockContext);
            });
            assertNotNull(exception);
    }


}