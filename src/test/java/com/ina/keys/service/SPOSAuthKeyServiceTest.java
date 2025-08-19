package com.ina.keys.service;

import com.ina.CommonObjects;
import com.ina.common.config.AppContext;
import com.ina.common.crypto.model.certs.ServerCertsGenerationResponse;
import com.ina.common.crypto.model.keys.FetchSPOSAuthKeyRequest;
import com.ina.common.crypto.model.keys.SPOSAuthKeyInfo;
import com.ina.common.crypto.model.keys.SPOSAuthKeyRequest;
import com.ina.common.crypto.service.SPOSAuthenticationKeys;
import com.ina.common.crypto.util.CryptoUtils;
import com.ina.common.enums.NextCommandDetails;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiInContext;
import com.ina.common.model.CommonResponse;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.utils.CommonUtils;
import com.ina.common.validator.DeviceProfileValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SPOSAuthKeyServiceTest extends CommonObjects {

    @InjectMocks
    private SPOSAuthKeyService sposAuthKeyService;

    @Mock
    private SPOSAuthenticationKeys sposAuthenticationKeys;

    @Mock
    private CryptoUtils cryptoUtils;

    @Mock
    private InaPayMessages inaPayMessages;

    @Mock
    private ApiInContext apiInContext;

    @Mock
    private DeviceProfileValidator deviceProfileValidator;

    @Mock
    private CommonUtils commonUtils;


    @Test
    public void testInitialiseIsInjected() {
        assertNotNull(sposAuthKeyService);
    }



    @Test
    void testGenerateSPOSAuthKeySuccess() {
        SPOSAuthKeyRequest request = new SPOSAuthKeyRequest();
        request.setAuthKeyType("ECDSA");
        request.setApiInContext(CommonObjects.getApiInContext());
        ServerCertsGenerationResponse responseMock = new ServerCertsGenerationResponse();
        responseMock.setStatusCode("200");
        when(cryptoUtils.updateServerCertificates(any(), anyString(), any()))
                .thenReturn(responseMock);
        when(inaPayMessages.get("000")).thenReturn("SUCCESS");
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse response = sposAuthKeyService.generateSPOSAuthKey(request);
            assertEquals("1234", response.getApiOutContext().getOutputRefId());

        }
    }

    @Test
    void testGenerateSPOSAuthKeyFailure() {
        SPOSAuthKeyRequest request = new SPOSAuthKeyRequest();
        request.setAuthKeyType("ECDSA");
        request.setApiInContext(CommonObjects.getApiInContext());
        ServerCertsGenerationResponse responseMock = new ServerCertsGenerationResponse();
        responseMock.setStatusCode("400");
        when(cryptoUtils.updateServerCertificates(any(), anyString(), any()))
                .thenReturn(responseMock);
        when(inaPayMessages.get("999"))
                .thenReturn("Failed");
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse response = sposAuthKeyService.generateSPOSAuthKey(request);
            assertNotNull(response);
        }
    }
    @Test
    void testGenerateSPOSAuthKeyException() {
        SPOSAuthKeyRequest request = new SPOSAuthKeyRequest();
        request.setAuthKeyType("ECDSA");
        request.setApiInContext(CommonObjects.getApiInContext());

        when(cryptoUtils.updateServerCertificates(any(), anyString(), any()))
                .thenThrow(new CommonValidationException("E101", "VALIDATION_FAILED", "Invalid certificate", NextCommandDetails.BLOCK, null));
        when(inaPayMessages.get("999")).thenReturn("Failed");
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {

            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse response = sposAuthKeyService.generateSPOSAuthKey(request);

            assertNotNull(response);
        }
    }



    @Test
    public void testGetSPOSAuthKeySuccess() {
        FetchSPOSAuthKeyRequest fetchSPOSAuthKeyRequest=new FetchSPOSAuthKeyRequest();
        fetchSPOSAuthKeyRequest.setAuthKeyType("ECDSA");
        fetchSPOSAuthKeyRequest.setApiInContext(CommonObjects.getApiInContext());
        SPOSAuthKeyInfo sposAuthKeyInfo=new SPOSAuthKeyInfo();
        sposAuthKeyInfo.setStatusCode("200");
        when(sposAuthenticationKeys.getSPOSAuthenticationKeys("ECDSA", "1234"))
                .thenReturn(sposAuthKeyInfo);
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse result = sposAuthKeyService.getSPOSAuthKey(fetchSPOSAuthKeyRequest);
            assertNotNull(result);


        }

    }

    @Test
    void testGetSPOSAuthKeyFailure() {
        FetchSPOSAuthKeyRequest fetchSPOSAuthKeyRequest = new FetchSPOSAuthKeyRequest();
        fetchSPOSAuthKeyRequest.setAuthKeyType("ECDSA");
        fetchSPOSAuthKeyRequest.setApiInContext(CommonObjects.getApiInContext());
        SPOSAuthKeyInfo sposAuthKeyInfo = new SPOSAuthKeyInfo();
        sposAuthKeyInfo.setStatusCode("400");
        when(sposAuthenticationKeys.getSPOSAuthenticationKeys("ECDSA", "1234"))
                .thenReturn(sposAuthKeyInfo);
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse result = sposAuthKeyService.getSPOSAuthKey(fetchSPOSAuthKeyRequest);
            assertNotNull(result);

        }
    }

    @Test
    void testGetSPOSAuthKeyException() {
        FetchSPOSAuthKeyRequest fetchSPOSAuthKeyRequest = new FetchSPOSAuthKeyRequest();
        fetchSPOSAuthKeyRequest.setAuthKeyType("ECDSA");
        fetchSPOSAuthKeyRequest.setApiInContext(CommonObjects.getApiInContext());
        SPOSAuthKeyInfo sposAuthKeyInfo = new SPOSAuthKeyInfo();
        sposAuthKeyInfo.setStatusCode("400");
        when(sposAuthenticationKeys.getSPOSAuthenticationKeys("ECDSA", "1234"))
                .thenReturn(sposAuthKeyInfo);

        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            CommonResponse result = sposAuthKeyService.getSPOSAuthKey(fetchSPOSAuthKeyRequest);

            assertNotNull(result);
        }
    }

    @Test
    public void testGenerateDEKAndAEKKeyException() {
        FetchSPOSAuthKeyRequest fetchSPOSAuthKeyRequest = new FetchSPOSAuthKeyRequest();
        fetchSPOSAuthKeyRequest.setAuthKeyType("ECDSA");
        fetchSPOSAuthKeyRequest.setApiInContext(CommonObjects.getApiInContext());

        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            when(sposAuthenticationKeys.getSPOSAuthenticationKeys("ECDSA", "1234"))
                    .thenThrow(new CommonValidationException("123", "ERR_CODE", "Some error", NextCommandDetails.BLOCK, null));


            CommonResponse result = sposAuthKeyService.getSPOSAuthKey(fetchSPOSAuthKeyRequest);
            assertNotNull(result);
        }
    }

    @Test
    void testEvaluate() {
        SPOSAuthKeyRequest sposAuthKeyRequest=new SPOSAuthKeyRequest();
        sposAuthKeyRequest.setApiInContext(buildApiInContext());
        try (MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(getApiOutContextData());
            sposAuthKeyService.evaluate(sposAuthKeyRequest);
        }
    }
    @Test
    void testEvaluate_throwsException_whenTimeStampIsNull() {

        ApiInContext mockContext = mock(ApiInContext.class);
        when(mockContext.getInputRefId()).thenReturn("input-123");
        when(mockContext.getTimeStamp()).thenReturn(null);
        SPOSAuthKeyRequest sposAuthKeyRequest=new SPOSAuthKeyRequest();
        sposAuthKeyRequest.setApiInContext(mockContext);
        InaPayMessages mockMessages = mock(InaPayMessages.class);
        ReflectionTestUtils.setField(sposAuthenticationKeys, "messages", mockMessages);

        Exception exception = assertThrows(Exception.class, () -> {
            sposAuthKeyService.evaluate(sposAuthKeyRequest);
        });
        assertNotNull(exception);
    }
}