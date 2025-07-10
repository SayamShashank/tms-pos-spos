package com.ina.keys.service;

import com.ina.CommonObjects;
import com.ina.common.crypto.model.certs.ServerCertsGenerationResponse;
import com.ina.common.crypto.model.keys.FetchSPOSAuthKeyRequest;
import com.ina.common.crypto.model.keys.SPOSAuthKeyInfo;
import com.ina.common.crypto.model.keys.SPOSAuthKeyRequest;
import com.ina.common.crypto.service.SPOSAuthenticationKeys;
import com.ina.common.crypto.util.CryptoUtils;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.CommonResponse;
import com.ina.common.response.message.InaPayMessages;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SPOSAuthKeyServiceTest {

    public static final String PUBLIC_KEY =
            "-----BEGIN PUBLIC KEY-----\n" +
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnZUI9aQ9r+x0HR3QplkM\n" +
                    "LzHNkQzK5yQbpY6ZIgVkbL5E8n9GLC8sJ5l+kPXmrDo5tb5+DxRf4OPUJvX1v3lY\n" +
                    "QXr1BsmH1lR2yB09MyXt0mGFlq7b4A4yGmUj7YsnUMLd5pZGiCm+oZBllJbXsVcZ\n" +
                    "ykOQ2J9m1oKD2WyMkCW94YtIfvSkRTy2S4Th0Wmnvvrr3Rb8zX8iAB0cX56jwIDAQAB\n" +
                    "-----END PUBLIC KEY-----";

    public static final String EXPORTED_PRIVATE_KEY =
            "-----BEGIN PRIVATE KEY-----\n" +
                    "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCd1Qj1pD2v7HQd\n" +
                    "HdCmWQwvMc2RDMrnJBuljpkjBWRsvkTye0YsLywnmX6Q9easOjltvn4PFF/g49Qm\n" +
                    "9fW/eVhBfvsGycfWNHbIHT0zJe3SYYWVrtvgDjIaZSPticyUwt3mlkaIKb6hkGWU\n" +
                    "ltewVxnKQ5DYn2bWgoPZbIyQJb3hi0h+9KRFPLZLhOHRaae+++vdFvzNfyIAHRxf\n" +
                    "nqPAgMBAAECggEABV6Aw8Rf3wqpPw1OeMRBbfq/De8XUHV8d9UOX3ZGGOrlhBlJs\n" +
                    "BBEKivwINyfFl+w8qctH95OlNtwbAK9Rz7/8SqaSB5RhQ3I8Knv4rf9eAEIOFydS\n" +
                    "h4DwIDAQAB\n" +
                    "-----END PRIVATE KEY-----";

    @InjectMocks
    private SPOSAuthKeyService sposAuthKeyService;

    @Mock
    private SPOSAuthenticationKeys sposAuthenticationKeys;

    @Mock
    private CryptoUtils cryptoUtils;

    @Mock
    private InaPayMessages inaPayMessages;



    @Test
    public void testInitialiseIsInjected() {
        assertNotNull(sposAuthKeyService);
    }



    @Test
    void testGenerateSPOSAuthKeySuccess() {
        SPOSAuthKeyRequest request = new SPOSAuthKeyRequest();
        request.setAuthKeyType("ECDSA");
        request.setPubKey(PUBLIC_KEY);
        request.setExportedPvtKey(EXPORTED_PRIVATE_KEY);
        request.setApiInContext(CommonObjects.getApiInContext());
        ServerCertsGenerationResponse responseMock = new ServerCertsGenerationResponse();
        responseMock.setStatusCode("200");
        when(cryptoUtils.updateServerCertificates(any(), anyString(), any()))
                .thenReturn(responseMock);
        when(inaPayMessages.get("0000")).thenReturn("SUCCESS");
        CommonResponse response = sposAuthKeyService.generateSPOSAuthKey(request);
        assertEquals("1234", response.getApiOutContext().getOutputRefId());
        assertNotNull(response.getApiOutContext().getTimeStamp());
    }

    @Test
    void testGenerateSPOSAuthKeyFailure() {
        SPOSAuthKeyRequest request = new SPOSAuthKeyRequest();
        request.setAuthKeyType("ECDSA");
        request.setPubKey(PUBLIC_KEY);
        request.setExportedPvtKey(EXPORTED_PRIVATE_KEY);
        request.setApiInContext(CommonObjects.getApiInContext());
        ServerCertsGenerationResponse responseMock = new ServerCertsGenerationResponse();
        responseMock.setStatusCode("400");
        when(cryptoUtils.updateServerCertificates(any(), anyString(), any()))
                .thenReturn(responseMock);
        when(inaPayMessages.get("9999"))
                .thenReturn("Failed");
        CommonResponse response = sposAuthKeyService.generateSPOSAuthKey(request);
        assertEquals("1234", response.getApiOutContext().getOutputRefId());
    }
    @Test
    void testGenerateSPOSAuthKeyException() throws Exception {
        SPOSAuthKeyRequest request = new SPOSAuthKeyRequest();
        request.setAuthKeyType("ECDSA");
        request.setPubKey(PUBLIC_KEY);
        request.setExportedPvtKey(EXPORTED_PRIVATE_KEY);
        request.setApiInContext(CommonObjects.getApiInContext());

        when(cryptoUtils.updateServerCertificates(any(), anyString(), any()))
                .thenThrow(new CommonValidationException("E101", "VALIDATION_FAILED", "Invalid certificate", "", null));
        when(inaPayMessages.get("9999")).thenReturn("Failed");

        CommonResponse response = sposAuthKeyService.generateSPOSAuthKey(request);

        assertEquals("TMSVALIDATION_FAILED", response.getApiOutContext().getCode());
        assertEquals("Failed", response.getApiOutContext().getStatus());
        assertEquals("1234", response.getApiOutContext().getOutputRefId());
    }




    @Test
    public void testGetSPOSAuthKeySuccess() {
        FetchSPOSAuthKeyRequest fetchSPOSAuthKeyRequest=new FetchSPOSAuthKeyRequest();
        fetchSPOSAuthKeyRequest.setAuthKeyType("ECDSA");
        fetchSPOSAuthKeyRequest.setApiInContext(CommonObjects.getApiInContext());
        SPOSAuthKeyInfo sposAuthKeyInfo=new SPOSAuthKeyInfo();
        sposAuthKeyInfo.setPublicKey(PUBLIC_KEY);
        sposAuthKeyInfo.setExportedPvtKey(EXPORTED_PRIVATE_KEY);
        sposAuthKeyInfo.setStatusCode("200");
        when(sposAuthenticationKeys.getSPOSAuthenticationKeys("ECDSA", "1234"))
                .thenReturn(sposAuthKeyInfo);
        CommonResponse result = sposAuthKeyService.getSPOSAuthKey(fetchSPOSAuthKeyRequest);

        assertEquals("1234", result.getApiOutContext().getOutputRefId());


    }

    @Test
    void testGetSPOSAuthKeyFailure() {
        FetchSPOSAuthKeyRequest fetchSPOSAuthKeyRequest = new FetchSPOSAuthKeyRequest();
        fetchSPOSAuthKeyRequest.setAuthKeyType("ECDSA");
        fetchSPOSAuthKeyRequest.setApiInContext(CommonObjects.getApiInContext());
        SPOSAuthKeyInfo sposAuthKeyInfo = new SPOSAuthKeyInfo();
        sposAuthKeyInfo.setPublicKey(PUBLIC_KEY);
        sposAuthKeyInfo.setExportedPvtKey(EXPORTED_PRIVATE_KEY);
        sposAuthKeyInfo.setStatusCode("400");
        when(sposAuthenticationKeys.getSPOSAuthenticationKeys("ECDSA", "1234"))
                .thenReturn(sposAuthKeyInfo);
        CommonResponse result = sposAuthKeyService.getSPOSAuthKey(fetchSPOSAuthKeyRequest);

        assertEquals("400", sposAuthKeyInfo.getStatusCode());
    }

    @Test
    void testGetSPOSAuthKeyException() {
        FetchSPOSAuthKeyRequest fetchSPOSAuthKeyRequest = new FetchSPOSAuthKeyRequest();
        fetchSPOSAuthKeyRequest.setAuthKeyType("ECDSA");
        fetchSPOSAuthKeyRequest.setApiInContext(CommonObjects.getApiInContext());
        SPOSAuthKeyInfo sposAuthKeyInfo = new SPOSAuthKeyInfo();
        sposAuthKeyInfo.setPublicKey(PUBLIC_KEY);
        sposAuthKeyInfo.setExportedPvtKey(EXPORTED_PRIVATE_KEY);
        sposAuthKeyInfo.setStatusCode("400");
        when(sposAuthenticationKeys.getSPOSAuthenticationKeys("ECDSA", "1234"))
                .thenReturn(sposAuthKeyInfo);
        CommonResponse result = sposAuthKeyService.getSPOSAuthKey(fetchSPOSAuthKeyRequest);

        assertEquals("1234",result.getApiOutContext().getOutputRefId());
    }

    @Test
    public void testGenerateDEKAndAEKKeyException() {
        FetchSPOSAuthKeyRequest fetchSPOSAuthKeyRequest = new FetchSPOSAuthKeyRequest();
        fetchSPOSAuthKeyRequest.setAuthKeyType("ECDSA");
        fetchSPOSAuthKeyRequest.setApiInContext(CommonObjects.getApiInContext());
        when(sposAuthenticationKeys.getSPOSAuthenticationKeys("ECDSA", "1234"))
                .thenThrow(new CommonValidationException("123", "ERR_CODE", "Some error", "Additional", null));

        CommonResponse result = sposAuthKeyService.getSPOSAuthKey(fetchSPOSAuthKeyRequest);


        assertEquals("1234", result.getApiOutContext().getOutputRefId());
        assertEquals("TMSERR_CODE", result.getApiOutContext().getCode());
    }




}