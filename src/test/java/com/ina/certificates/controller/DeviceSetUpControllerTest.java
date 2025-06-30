package com.ina.certificates.controller;

import com.ina.certificates.model.DeviceTMSInitRequest;
import com.ina.certificates.model.DeviceTMSInitResponse;
import com.ina.certificates.service.DeviceSetUpService;
import com.ina.common.model.ApiOutContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ina.CommonObjects.getApiInContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceSetUpControllerTest {

    @InjectMocks
    private DeviceSetUpController deviceSetUpController;

    @Mock
    DeviceSetUpService deviceSetUpService;

    @BeforeEach
    void setup(){
        deviceSetUpController=new DeviceSetUpController(deviceSetUpService);

    }

    @Test
    void contextLoads(){
        assertNotNull(deviceSetUpController);
    }

    @Test
    void deviceTMSInitApiTest(){
        DeviceTMSInitRequest request = new DeviceTMSInitRequest();
        request.setApiInContext(getApiInContext());
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        DeviceTMSInitResponse deviceTMSInitResponse = new DeviceTMSInitResponse();
        deviceTMSInitResponse.setApiOutContext(apiOutContext);
        when(deviceSetUpService.deviceTMSInit(request)).thenReturn(deviceTMSInitResponse);
        DeviceTMSInitResponse response = deviceSetUpController.deviceTMSInit(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(), response.getApiOutContext().getOutputRefId());
    }
}