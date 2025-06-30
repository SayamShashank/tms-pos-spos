package com.ina.certificates.service;

import com.ina.CommonObjects;
import com.ina.common.crypto.model.certs.*;
import com.ina.common.crypto.service.CertGenerationService;
import com.ina.common.crypto.service.ServerEncryptionAndSignatureCertificateService;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiInContext;
import com.ina.common.model.ApiOutContext;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.Request;
import com.ina.common.response.message.InaPayMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;

import static com.ina.CommonObjects.createRequest;
import static com.ina.CommonObjects.getApiInContext;
import static com.ina.common.constants.AppErrorConstants.FAILED_CODE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InitialiseCertificateServiceTest {

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

    @BeforeEach
    void setUp() {
        apiInContext = new ApiInContext();
        apiInContext.setInputRefId("ref123");
        apiInContext.setTimeStamp("2024-01-01T00:00:00");
        initialiseCertRequest = new InitialiseCertRequest();
        initialiseCertRequest.setApiInContext(apiInContext);
        initialiseCertRequest.setCertificateType("SIGN");
        request = new Request();
        request.setApiInContext(apiInContext);
    }

    @Test
    void testGenerateServerCerts_Success() {
        when(messages.get("0000")).thenReturn("Success");
        CertificateGenerationResponse certResponse = new CertificateGenerationResponse();
        certResponse.setStatus("success");

        when(certGenerationService.generateCertificate(eq("SIGN"), any())).thenReturn(certResponse);

        CommonResponse response = initialiseCertificateService.generateServerCerts(initialiseCertRequest);
        assertNotNull(response);
    }

    @Test
    void testGenerateServerCerts_Failure() {
        when(messages.get("9999")).thenReturn("Failed");
        CertificateGenerationResponse certResponse = new CertificateGenerationResponse();
        certResponse.setStatus("failure");
        certResponse.setMessage("Failed");

        when(certGenerationService.generateCertificate(eq("SIGN"), any())).thenReturn(certResponse);

        CommonResponse response = initialiseCertificateService.generateServerCerts(initialiseCertRequest);
        assertNotNull(response);
    }

    @Test
    void testGenerateServerCerts_Exception() {
        when(certGenerationService.generateCertificate(eq("SIGN"), any()))
                .thenThrow(new CommonValidationException("ERR001", "Validation error"));

        CommonResponse response = initialiseCertificateService.generateServerCerts(initialiseCertRequest);
        assertNotNull(response);
    }

    @Test
    void testGenerateRootCertificate_Success() {
        when(messages.get("0000")).thenReturn("Success");
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

        CommonResponse response = initialiseCertificateService.generateRootCertificate(publishRootCertificateRequest);
        assertNotNull(response);
    }

    @Test
    void testGenerateRootCertificateFailure() {
        when(messages.get("9999")).thenReturn("Failed");
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

        CommonResponse response = initialiseCertificateService.generateRootCertificate(publishRootCertificateRequest);
        assertNotNull(response);
    }


    @Test
    void testGenerateRootCertificateException() {
        PublishRootCertificateRequest publishRootCertificateRequest=new PublishRootCertificateRequest();
        publishRootCertificateRequest.setCertificateType(" ");
        RootCertificateResponse rootResponse = new RootCertificateResponse();
        rootResponse.setCert("cert-data");
        rootResponse.setSkLmk("key-data");
        publishRootCertificateRequest.setRootCertificateResponse(rootResponse);
        publishRootCertificateRequest.setApiInContext(getApiInContext());
        when(certGenerationService.saveCertificatesIntoDB(any(),any(), any()))
                .thenThrow(new CommonValidationException("ERR001", "Validation error"));
        CommonResponse response = initialiseCertificateService.generateRootCertificate(publishRootCertificateRequest);
        assertNotNull(response);
    }

    @Test
    void testGenerateL4Certs_Success() {
        when(messages.get("0000")).thenReturn("Success");
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
        when(messages.get("0000")).thenReturn("Failed");
        ApiOutContext outContext = new ApiOutContext();
        outContext.setStatus("Failure");

        ServerCertsInfoResponse serverCertsInfoResponse = new ServerCertsInfoResponse();
        serverCertsInfoResponse.setApiOutContext(outContext);

        when(serverEncryptionAndSignatureCertificateService
                .generateServerL4Certificates(any(), any())).thenReturn(serverCertsInfoResponse);

        CommonResponse response = initialiseCertificateService.generateL4Certs(initialiseCertRequest);
        assertNotNull(response);
    }

    @Test
    void testGenerateL4CertsException(){
        when(serverEncryptionAndSignatureCertificateService.generateServerL4Certificates(any(), any()))
                .thenThrow(new CommonValidationException("ERR001", "Validation error"));
        CommonResponse response = initialiseCertificateService.generateL4Certs(initialiseCertRequest);
        assertNotNull(response);
    }

    @Test
    void testGetAllServerCerts() {
        when(messages.get("0000")).thenReturn("Success");
        ServerCertsResponse certsResponse = new ServerCertsResponse();
        when(certGenerationService.getAllServerCerts()).thenReturn(certsResponse);

        ServerCertsResponse response = initialiseCertificateService.getAllServerCerts(request);
        assertEquals("Success", response.getApiOutContext().getMessage());
    }

    @Test
    void testViewCertInfo_success() {
        InitialiseCertRequest request = createRequest();

        ViewCertificateInfo mockCertInfo = new ViewCertificateInfo();
        mockCertInfo.setCertSerialNumber("12345");
        mockCertInfo.setCertType("L4");
        mockCertInfo.setKeyExpiry(new Timestamp(System.currentTimeMillis()));
        mockCertInfo.setEnteredBy("admin");
        mockCertInfo.setEntryDate(new Timestamp(System.currentTimeMillis()));

        when(certGenerationService.viewCertificate("L4", "ref123"))
                .thenReturn(mockCertInfo);


        InitialiseCertificateService spyService = spy(initialiseCertificateService);

        ViewCertificateInfo result = spyService.viewCertInfo(request);

        assertNotNull(result);
        assertEquals("12345", result.getCertSerialNumber());
        assertEquals("L4", result.getCertType());

        assertNotNull(result.getApiOutContext());


        assertEquals("ref123", result.getApiOutContext().getOutputRefId());
    }

    @Test
    void testViewCertInfo_certNotAvailable() {

        ViewCertificateInfo mockCertInfo = new ViewCertificateInfo();
        mockCertInfo.setCertSerialNumber(null);
        mockCertInfo.setCertType("L4");
        mockCertInfo.setKeyExpiry(new Timestamp(System.currentTimeMillis()));
        mockCertInfo.setEnteredBy("admin");
        mockCertInfo.setEntryDate(new Timestamp(System.currentTimeMillis()));
        mockCertInfo.setApiOutContext(CommonObjects.getApiOutContextDataForCertificate());

        when(certGenerationService.viewCertificate("L4", "ref123")).thenReturn(mockCertInfo);


        ViewCertificateInfo result = certGenerationService.viewCertificate("L4", "ref123");

        assertNotNull(result);
        assertNull(result.getCertSerialNumber());
        assertNotNull(result.getApiOutContext());

        assertEquals(FAILED_CODE, result.getApiOutContext().getStatus());

        assertEquals("ref123", result.getApiOutContext().getOutputRefId());
    }


    @Test
    void testViewCertInfoException() {
        when(certGenerationService.viewCertificate(anyString(),anyString()))
                .thenThrow(new CommonValidationException("ERR001", "Validation error"));
        CommonResponse response = initialiseCertificateService.viewCertInfo(initialiseCertRequest);
        assertNotNull(response);
    }

    @Test
    void testGetServerCertificateStatusSuccess() {
        InitialiseCertRequest certRequest = new InitialiseCertRequest();
        certRequest.setApiInContext(getApiInContext());
        certRequest.setCertificateType("SSL");
        ServerCertsStatusResponse certsStatusResponse = new ServerCertsStatusResponse();
        when(certGenerationService.getServerCertificateStatus("SSL"))
                .thenReturn(certsStatusResponse);
        when(messages.get("0000")).thenReturn("Success");

        ServerCertsStatusResponse result = initialiseCertificateService.getServerCertificateStatus(certRequest);
        assertNotNull(result);
        assertNotNull(result.getApiOutContext());
        assertEquals("1234", result.getApiOutContext().getOutputRefId());
        assertEquals("0000", result.getApiOutContext().getCode());
        assertEquals("Success", result.getApiOutContext().getMessage());
    }
}