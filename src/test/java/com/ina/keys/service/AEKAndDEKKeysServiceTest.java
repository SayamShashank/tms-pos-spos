package com.ina.keys.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AEKAndDEKKeysServiceTest {

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


    @BeforeEach
    void setUp() {
        request = new Request();
        ApiInContext context = new ApiInContext();
        context.setInputRefId("123");
        request.setApiInContext(context);
        when(messages.get(anyString())).thenReturn("OK");
    }

    @Test
    void testGenerateDEKAndAEKKeySuccess() {
        GenerateAEKAndDEKInfo info = new GenerateAEKAndDEKInfo("200", "Success");
        GenerateAEKAndDEKResponse response = new GenerateAEKAndDEKResponse(List.of(info));
        when(aekAndDEKService.generateAEKAndDEK("TMS", "123")).thenReturn(response);
        CommonResponse result = aekAndDEKKeysService.generateDEKAndAEKKey(request);
        assertEquals("OK", result.getApiOutContext().getStatus());
        assertEquals("123", result.getApiOutContext().getOutputRefId());
    }

    @Test
    void testGenerateDEKAndAEKKeyFailure() {
        GenerateAEKAndDEKInfo info = new GenerateAEKAndDEKInfo("500", "Internal Error");
        GenerateAEKAndDEKResponse response = new GenerateAEKAndDEKResponse(List.of(info));
        when(aekAndDEKService.generateAEKAndDEK("TMS", "123")).thenReturn(response);
        CommonResponse result = aekAndDEKKeysService.generateDEKAndAEKKey(request);
        assertEquals("OK", result.getApiOutContext().getStatus());
        assertEquals("123", result.getApiOutContext().getOutputRefId());
    }

    @Test
    void testGenerateDEKAndAEKKeyException() {
        when(aekAndDEKService.generateAEKAndDEK("TMS", "123"))
                .thenThrow(new CommonValidationException("123", "ERR_CODE", "Some error", NextCommandDetails.BLOCK, null));
        CommonResponse result = aekAndDEKKeysService.generateDEKAndAEKKey(request);
        assertEquals("OK", result.getApiOutContext().getStatus());
        assertEquals("123", result.getApiOutContext().getOutputRefId());
        assertEquals("TMSERR_CODE", result.getApiOutContext().getCode());
    }

    @Test
    void testRotateDEKSuccess() {
        GenerateAEKAndDEKInfo info = new GenerateAEKAndDEKInfo("200", "Success");
        GenerateAEKAndDEKResponse response = new GenerateAEKAndDEKResponse(List.of(info));
        when(aekAndDEKService.rotateDEK("TMS", "123")).thenReturn(response);
        CommonResponse result = aekAndDEKKeysService.rotateDEK(request);
        assertEquals("OK", result.getApiOutContext().getStatus());
        assertEquals("123", result.getApiOutContext().getOutputRefId());
    }

    @Test
    void testRotateDEKFailure() {
        GenerateAEKAndDEKInfo info = new GenerateAEKAndDEKInfo("500", "Error");
        GenerateAEKAndDEKResponse response = new GenerateAEKAndDEKResponse(List.of(info));
        when(aekAndDEKService.rotateDEK("TMS", "123")).thenReturn(response);
        CommonResponse result = aekAndDEKKeysService.rotateDEK(request);
        assertEquals("OK", result.getApiOutContext().getStatus());
        assertEquals("123", result.getApiOutContext().getOutputRefId());
    }

    @Test
    void testRotateDEKException() {
        when(aekAndDEKService.rotateDEK("TMS", "123"))
                .thenThrow(new CommonValidationException("123", "ROTATE_ERR", "Rotate Error", NextCommandDetails.BLOCK, null));
        CommonResponse result = aekAndDEKKeysService.rotateDEK(request);
        assertEquals("OK", result.getApiOutContext().getStatus());
        assertEquals("TMSROTATE_ERR", result.getApiOutContext().getCode());
        assertEquals("123", result.getApiOutContext().getOutputRefId());
    }

    @Test
    void testGetAllAEKAndDEKServerKeysSuccess() {
        AvailableServerKeysResponse keysResponse = new AvailableServerKeysResponse();
        when(aekAndDEKService.getAvailableKeys()).thenReturn(keysResponse);
        AvailableServerKeysResponse result = aekAndDEKKeysService.getAllAEKAndDEkServerKeys(request);
        assertEquals("123", result.getApiOutContext().getOutputRefId());
        assertEquals("OK", result.getApiOutContext().getMessage());
    }
}
