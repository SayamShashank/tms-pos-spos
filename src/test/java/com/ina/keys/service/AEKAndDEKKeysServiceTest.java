package com.ina.keys.service;

import com.ina.CommonObjects;
import com.ina.common.config.AppContext;
import com.ina.common.crypto.model.aekdek.AvailableServerKeysResponse;
import com.ina.common.crypto.model.aekdek.GenerateAEKAndDEKInfo;
import com.ina.common.crypto.model.aekdek.GenerateAEKAndDEKResponse;
import com.ina.common.crypto.service.AEKAndDEKService;
import com.ina.common.enums.NextCommandDetails;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiInContext;
import com.ina.common.model.CommonResponse;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
public class AEKAndDEKKeysServiceTest extends CommonObjects {

    @Mock
    private AEKAndDEKService aekAndDEKService;

    @Mock
    private InaPayMessages messages;

    @InjectMocks
    private AEKAndDEKKeysService aekAndDEKKeysService;

    @Mock
    private Request request;

    @Mock
    private AvailableServerKeysResponse availableServerKeysResponse;


    @Mock
    private ApiInContext apiInContext;

    @Mock
    private DeviceProfileValidator deviceProfileValidator;

    @Mock
    private CommonUtils commonUtils;


    @BeforeEach
    void setUp() {
        request = new Request();
        ApiInContext context = new ApiInContext();
        context.setInputRefId("123");
        request.setApiInContext(context);

    }
    @Test
    void testGenerateDEKAndAEKKeySuccess() {
        GenerateAEKAndDEKInfo info = new GenerateAEKAndDEKInfo("200", "Success");
        GenerateAEKAndDEKResponse response = new GenerateAEKAndDEKResponse(List.of(info));
        when(aekAndDEKService.generateAEKAndDEK("TMS", "123")).thenReturn(response);

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
            CommonResponse result = aekAndDEKKeysService.generateDEKAndAEKKey(request);
            assertNotNull(result);
        }
    }

    @Test
    void testGenerateDEKAndAEKKeyFailure() {
        GenerateAEKAndDEKInfo info = new GenerateAEKAndDEKInfo("500", "Internal Error");
        GenerateAEKAndDEKResponse response = new GenerateAEKAndDEKResponse(List.of(info));
        when(aekAndDEKService.generateAEKAndDEK("TMS", "123")).thenReturn(response);
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
            CommonResponse result = aekAndDEKKeysService.generateDEKAndAEKKey(request);
            assertNotNull(result);
        }
    }

    @Test
    void testGenerateDEKAndAEKKeyException() {

        when(aekAndDEKService.generateAEKAndDEK("TMS", "123"))
                .thenThrow(new CommonValidationException("123", "ERR_CODE", "Some error", NextCommandDetails.BLOCK, null));
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

            CommonResponse result = aekAndDEKKeysService.generateDEKAndAEKKey(request);
            assertNotNull(result);
        }
    }

    @Test
    void testRotateDEKSuccess() {
        GenerateAEKAndDEKInfo info = new GenerateAEKAndDEKInfo("200", "Success");
        GenerateAEKAndDEKResponse response = new GenerateAEKAndDEKResponse(List.of(info));
        when(aekAndDEKService.rotateDEK("TMS", "123")).thenReturn(response);
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
            CommonResponse result = aekAndDEKKeysService.rotateDEK(request);
            assertNotNull(result);
        }
    }

    @Test
    void testRotateDEKFailure() {
        GenerateAEKAndDEKInfo info = new GenerateAEKAndDEKInfo("500", "Error");
        GenerateAEKAndDEKResponse response = new GenerateAEKAndDEKResponse(List.of(info));
        when(aekAndDEKService.rotateDEK("TMS", "123")).thenReturn(response);
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
            CommonResponse result = aekAndDEKKeysService.rotateDEK(request);
            assertNotNull(result);
        }
    }

    @Test
    void testRotateDEKException() {
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
            when(aekAndDEKService.rotateDEK("TMS", "123"))
                    .thenThrow(new CommonValidationException("123", "ROTATE_ERR", "Rotate Error", NextCommandDetails.BLOCK, null));
            CommonResponse result = aekAndDEKKeysService.rotateDEK(request);
            assertNotNull(result);
        }
    }

    @Test
    void testGetAllAEKAndDEKServerKeysSuccess() {

        AvailableServerKeysResponse keysResponse = new AvailableServerKeysResponse();
        when(aekAndDEKService.getAvailableKeys()).thenReturn(keysResponse);
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

        AvailableServerKeysResponse result = aekAndDEKKeysService.getAllAEKAndDEkServerKeys(request);
        assertNotNull(result);

    }

    }

    @Test
    void testEvaluate() {
        Request request=buildRequest();
        try (MockedStatic<AppContext> appContextMockedStatic = mockStatic(AppContext.class);
             MockedStatic<CommonUtils> commonUtilsMockedStatic = mockStatic(CommonUtils.class)) {

            appContextMockedStatic.when(AppContext::getApplicationName)
                    .thenReturn("ina-txn-service");

            commonUtilsMockedStatic.when(CommonUtils::applicationContextServerName)
                    .thenReturn("TXN");
            commonUtilsMockedStatic.when(() ->
                    CommonUtils.getApiOutContext(
                            anyString(), anyString(), any(InaPayMessages.class), anyString())
            ).thenReturn(getApiOutContextData());
            aekAndDEKKeysService.evaluate(request);
        }
    }
    @Test
    void testEvaluate_throwsException_whenTimeStampIsNull() {

        ApiInContext mockContext = mock(ApiInContext.class);
        when(mockContext.getInputRefId()).thenReturn("input-123");
        when(mockContext.getTimeStamp()).thenReturn(null);
        Request request=new Request();
        request.setApiInContext(mockContext);

        InaPayMessages mockMessages = mock(InaPayMessages.class);
        ReflectionTestUtils.setField(aekAndDEKKeysService, "messages", mockMessages);

        Exception ex = assertThrows(Exception.class, () -> {
            aekAndDEKKeysService.evaluate(request);
        });
    }
}
