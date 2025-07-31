package com.ina.certificates.service;

import com.ina.CommonObjects;
import com.ina.certificates.model.DeviceTMSInitRequest;
import com.ina.certificates.model.DeviceTMSInitResponse;
import com.ina.common.config.AppContext;
import com.ina.common.crypto.model.init.SignedCertMetadata;
import com.ina.common.crypto.repository.AppReleaseDetailsRepository;
import com.ina.common.crypto.service.InitService;
import com.ina.common.enums.CertTypeAndLevel;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiInContext;
import com.ina.common.model.DeviceMetadata;
import com.ina.common.model.Request;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.utils.CommonUtils;
import com.ina.common.validator.DeviceProfileValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static com.ina.CommonObjects.getApiInContext;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
class DeviceSetUpServiceTest extends CommonObjects{

    @Mock
    InitService initService;

    @Mock
    InaPayMessages inaPayMessages;


    @InjectMocks
    DeviceSetUpService deviceSetUpService;

    Request request;

    @Mock
    private ApiInContext apiInContext;

    @Mock
    private DeviceProfileValidator deviceProfileValidator;

    @Mock
    private CommonUtils commonUtils;

    @Mock
    private AppReleaseDetailsRepository appReleaseDetailsRepository;


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
        when(appReleaseDetailsRepository.findByIsExpiredFalse())
                .thenReturn(buildAppReleaseDetails());
        when(initService.initProcess(deviceTMSInitRequest.getCertCSRMetadata(), "TMS_INIT", deviceTMSInitRequest.getApiInContext().getInputRefId()
                , deviceTMSInitRequest.getDeviceMetadata().getDeviceId(),buildAppReleaseDetails().getEndDate()))
                .thenReturn(signedCertMetadata);
        try (MockedStatic<AppContext> appContextMockedStatic = mockStatic(AppContext.class);
             MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {

            appContextMockedStatic.when(AppContext::getApplicationName)
                    .thenReturn("ina-txn-service");

            commonUtilsMockedStatic.when(CommonUtils::applicationContextServerName)
                    .thenReturn("TXN");
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            DeviceTMSInitResponse deviceTMSInitResponse = deviceSetUpService.deviceTMSInit(deviceTMSInitRequest);
            assertNotNull(deviceTMSInitResponse);
        }

    }
    @Test
    void testDeviceTXNInit_exceptionThrown() {
        DeviceTMSInitRequest deviceTXNInitRequest = new DeviceTMSInitRequest();
        deviceTXNInitRequest.setDeviceMetadata(DeviceMetadata.builder()
                .deviceId("1234")
                .build());
        deviceTXNInitRequest.setApiInContext(getApiInContext());
        when(appReleaseDetailsRepository.findByIsExpiredFalse())
                .thenReturn(buildAppReleaseDetails());
        when(initService.initProcess(any(), eq(CertTypeAndLevel.TMS_INIT.getCertType()), anyString(), anyString(),anyString()))
                .thenThrow(new CommonValidationException("12345", "5002"));
        try (MockedStatic<AppContext> appContextMockedStatic = mockStatic(AppContext.class);
             MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {
            appContextMockedStatic.when(AppContext::getApplicationName)
                    .thenReturn("ina-txn-service");
            commonUtilsMockedStatic.when(CommonUtils::applicationContextServerName)
                    .thenReturn("TXN");
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(buildApiOutContextData());
            DeviceTMSInitResponse response = deviceSetUpService.deviceTMSInit(deviceTXNInitRequest);
            assertNotNull(response);
        }
    }

    @Test
    void testEvaluate_throwsException_whenTimeStampIsNull() {
        ApiInContext mockContext = mock(ApiInContext.class);
        when(mockContext.getInputRefId()).thenReturn("input-123");
        when(mockContext.getTimeStamp()).thenReturn(null);
        DeviceTMSInitRequest deviceTMSInitRequest=new DeviceTMSInitRequest();
        deviceTMSInitRequest.setApiInContext(mockContext);
        InaPayMessages mockMessages = mock(InaPayMessages.class);
        ReflectionTestUtils.setField(deviceSetUpService, "messages", mockMessages);

        Exception exception = assertThrows(Exception.class, () -> {
            deviceSetUpService.evaluate(deviceTMSInitRequest);
        });
        assertNotNull(exception);
    }
}
