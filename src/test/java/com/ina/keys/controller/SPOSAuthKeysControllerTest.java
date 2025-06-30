package com.ina.keys.controller;

import com.ina.common.crypto.model.keys.FetchSPOSAuthKeyRequest;
import com.ina.common.crypto.model.keys.SPOSAuthKeyInfo;
import com.ina.common.crypto.model.keys.SPOSAuthKeyRequest;
import com.ina.common.crypto.model.keys.SPOSAuthKeyResponse;
import com.ina.common.model.CommonResponse;
import com.ina.keys.service.SPOSAuthKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static com.ina.CommonObjects.getApiInContext;
import static com.ina.CommonObjects.getApiOutContextData;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class SPOSAuthKeysControllerTest {

    @Mock
    private SPOSAuthKeyService sposAuthKeyService;

    @InjectMocks
    private SPOSAuthKeysController sposAuthKeysController;

    private SPOSAuthKeyResponse sposAuthKeyResponse;

    @BeforeEach
    void setUp(){
        sposAuthKeyResponse = new SPOSAuthKeyResponse();
        SPOSAuthKeyInfo sposAuthKeyInfo = new SPOSAuthKeyInfo();
        sposAuthKeyInfo.setPublicKey("testPubKey");
        sposAuthKeyInfo.setStatusCode("1234");
        sposAuthKeyInfo.setExportedPvtKey("testExportedPvtKey");
        sposAuthKeyResponse.setSposAuthKeyInfo(sposAuthKeyInfo);
        sposAuthKeyResponse.setApiOutContext(getApiOutContextData());
    }

    @Test
    void testGenerateSPOSAuthKeys() {
        SPOSAuthKeyRequest sposAuthKeyRequest = new SPOSAuthKeyRequest();
        sposAuthKeyRequest.setAuthKeyType("testAuthKey");
        sposAuthKeyRequest.setPubKey("testPubKey");
        sposAuthKeyRequest.setExportedPvtKey("testExportedPvtKey");
        sposAuthKeyRequest.setApiInContext(getApiInContext());
        when(sposAuthKeyService.generateSPOSAuthKey(sposAuthKeyRequest)).thenReturn(sposAuthKeyResponse);
        CommonResponse response = sposAuthKeysController.generateSPOSAuthKeys(sposAuthKeyRequest);
        assertNotNull(response);
    }

    @Test
    void testGetAllSPOSKeys () {
        FetchSPOSAuthKeyRequest fetchSPOSAuthKeyRequest = new FetchSPOSAuthKeyRequest();
        fetchSPOSAuthKeyRequest.setAuthKeyType("testAuthKey");
        fetchSPOSAuthKeyRequest.setApiInContext(getApiInContext());
        when(sposAuthKeyService.getSPOSAuthKey(fetchSPOSAuthKeyRequest)).thenReturn(sposAuthKeyResponse);
        CommonResponse response= sposAuthKeysController.getAllSPOSKeys(fetchSPOSAuthKeyRequest);
        assertNotNull(response);

    }

}