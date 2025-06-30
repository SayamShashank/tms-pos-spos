package com.ina.device.controller;

import com.ina.CommonObjects;
import com.ina.common.device.model.DeviceProfileBlockRequest;
import com.ina.common.device.model.DeviceUnblockRequest;
import com.ina.common.device.service.DeviceProfileUpdateStatusService;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.Request;
import com.ina.device.DeviceProfileController;
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
class DeviceProfileControllerTest {

    @InjectMocks
    DeviceProfileController deviceProfileController;

    @Mock
    DeviceProfileUpdateStatusService deviceProfileUpdateStatusService;


    @BeforeEach
    void setup(){
        deviceProfileController=new DeviceProfileController(deviceProfileUpdateStatusService);
    }

    @Test
    void contextLoads(){
        assertNotNull(deviceProfileController);
    }


    @Test
    void testDeviceUnblock() {
        DeviceUnblockRequest request=CommonObjects.buildDeviceUnBlockRequest();
        when(deviceProfileController.deviceUnblock(request))
                .thenReturn(CommonObjects.commonResponse(request));
        CommonResponse response=deviceProfileUpdateStatusService.deviceUnblock(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(),response.getApiOutContext().getOutputRefId());
    }



    @Test
    void testUpdateDeviceRevokeStatus() {
        Request request=CommonObjects.buildRequest();
        when(deviceProfileController.updateDeviceRevokeStatus(request))
                .thenReturn(CommonObjects.commonResponse(request));
        CommonResponse response=deviceProfileUpdateStatusService.updateDeviceInitAndRevokeStatus(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(),response.getApiOutContext().getOutputRefId());

    }

    @Test
    void testDeviceBlock(){

        DeviceProfileBlockRequest request=CommonObjects.buildDeviceBlockRequest();
        when(deviceProfileController.deviceBlock(request))
                .thenReturn(CommonObjects.commonResponse(request));
        CommonResponse response=deviceProfileUpdateStatusService.deviceBlockAndReset(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(),response.getApiOutContext().getOutputRefId());

    }
 }
