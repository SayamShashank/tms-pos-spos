package com.ina.keys.controller;

import com.ina.CommonObjects;
import com.ina.common.crypto.model.aekdek.AvailableServerKeysResponse;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.Request;
import com.ina.keys.service.AEKAndDEKKeysService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class AEKAndDEKKeysControllerTest {

    @InjectMocks
    AEKAndDEKKeysController aekAndDEKKeysController;

    @Mock
    AEKAndDEKKeysService aekAndDEKKeysService;


    @BeforeEach
    void setup(){
        aekAndDEKKeysController=new AEKAndDEKKeysController(aekAndDEKKeysService);

    }

    @Test
    void contextLoads(){
        assertNotNull(aekAndDEKKeysController);
    }

    @Test
    void testGenerateDEKAndAEKKeyApi(){
        Request request=CommonObjects.buildRequest();

        when(aekAndDEKKeysService.generateDEKAndAEKKey(request))
                .thenReturn(CommonObjects.commonResponse(request));

        CommonResponse response=aekAndDEKKeysController.generateDEKAndAEKKey(request);
        assertNotNull(response);

        assertEquals(request.getApiInContext().getInputRefId(),response.getApiOutContext().getOutputRefId());

    }

    @Test
    void  testRotateDEKAndAEKKeyApi(){
        Request request=CommonObjects.buildRequest();
        when(aekAndDEKKeysService.rotateDEK(request))
                .thenReturn(CommonObjects.commonResponse(request));
        CommonResponse response=aekAndDEKKeysController.rotateDEKAndAEKKey(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(),response.getApiOutContext().getOutputRefId());

    }

        @Test
        void testGetAllAEKAndDEkServerKeys() {
            Request request=CommonObjects.buildRequest();

        when(aekAndDEKKeysService.getAllAEKAndDEkServerKeys(request))
                .thenReturn(CommonObjects.getDummyAvailableServerKeysResponse());
        AvailableServerKeysResponse response=aekAndDEKKeysController.getAllAEKAndDEkServerKeys(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(),response.getApiOutContext().getOutputRefId());
        }
    }











