package com.ina.certificates.service;

import com.ina.CommonObjects;
import com.ina.certificates.model.DeviceTMSInitRequest;
import com.ina.certificates.model.DeviceTMSInitResponse;
import com.ina.common.crypto.model.init.SignedCertMetadata;
import com.ina.common.crypto.service.InitService;
import com.ina.common.enums.CertTypeAndLevel;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiInContext;
import com.ina.common.model.DeviceMetadata;
import com.ina.common.model.Request;
import com.ina.common.response.message.InaPayMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ina.CommonObjects.getApiInContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DeviceSetUpServiceTest {

    @Mock
    InitService initService;

    @Mock
    InaPayMessages inaPayMessages;


    @InjectMocks
    DeviceSetUpService deviceSetUpService;

    Request request;

    @BeforeEach
    public void setUp() {
        request = new Request();
        ApiInContext context = new ApiInContext();
        context.setInputRefId("123");
        request.setApiInContext(context);

    }

    @Test
    public void testInitialiseIsInjected() {

        assertNotNull(deviceSetUpService);
    }

    @Test
    public void deviceTMSInitSuccess() {
        SignedCertMetadata signedCertMetadata = CommonObjects.buildDeviceTMSInitResponse().getSignedCertMetadata();
        DeviceTMSInitRequest deviceTMSInitRequest = CommonObjects.buildDeviceTMSInitRequest();

        when(initService.initProcess(deviceTMSInitRequest.getCertCSRMetadata(), "TMS_INIT", deviceTMSInitRequest.getApiInContext().getInputRefId(),deviceTMSInitRequest.getDeviceMetadata().getDeviceId()))
                .thenReturn(signedCertMetadata);
        DeviceTMSInitResponse deviceTMSInitResponse = deviceSetUpService.deviceTMSInit(deviceTMSInitRequest);
        assertEquals(deviceTMSInitRequest.getApiInContext().getInputRefId(), deviceTMSInitResponse.getApiOutContext().getOutputRefId());
    }


    @Test
    void testDeviceTXNInit_exceptionThrown() {
        DeviceTMSInitRequest deviceTXNInitRequest=new DeviceTMSInitRequest();
        deviceTXNInitRequest.setDeviceMetadata(DeviceMetadata.builder()
                        .deviceId("1234")
                .build());
        deviceTXNInitRequest.setApiInContext(getApiInContext());
        when(initService.initProcess(any(), eq(CertTypeAndLevel.TMS_INIT.getCertType()), anyString(),anyString()))
                .thenThrow(new CommonValidationException("12345", "5002"));
        DeviceTMSInitResponse response = deviceSetUpService.deviceTMSInit(deviceTXNInitRequest);
        assertNotNull(response);
        assertNotNull(response.getApiOutContext());
        assertEquals("TMS5002", response.getApiOutContext().getCode());
        assertNull(response.getSignedCertMetadata());
    }
}