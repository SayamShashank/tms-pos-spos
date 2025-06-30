package com.ina.parameters.service;

import com.ina.CommonObjects;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiInContext;
import com.ina.common.model.DeviceMetadata;
import com.ina.common.response.message.InaPayMessages;
import com.ina.dao.EMVParametersRepository;
import com.ina.dao.entity.EMVParameters;
import com.ina.parameters.model.GetParamChecksumRequest;
import com.ina.parameters.model.ParamChecksumResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;

import static com.ina.constants.AppErrorConstants.CHECKSUM_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

class GetParamChecksumServiceTest extends CommonObjects {
    @Mock
    EMVParametersRepository emvParametersRepository;
    @Mock
    InaPayMessages inaPayMessages;
    @InjectMocks
    GetParamChecksumService getParamChecksumService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetParamChecksum() {
        GetParamChecksumRequest request=new GetParamChecksumRequest();
        request.setMid("mid");
        request.setTid("tid");
        request.setTrsMid("trsmid");
        request.setApiInContext(ApiInContext.builder()
                        .timeStamp(String.valueOf(new Timestamp(System.currentTimeMillis())))
                .build());
        request.setDeviceMetadata(DeviceMetadata.builder()
                        .deviceId("1234")
                .build());
        EMVParameters emvParameters = new EMVParameters(Long.valueOf(1), "merchantId", "terminalId", "trsMid", "deviceId", "cpks", "aids", "terminalConfig", "paramCheckSum", new Timestamp(0, 0, 0, 0, 0, 0, 0), new Timestamp(0, 0, 0, 0, 0, 0, 0));
        when(emvParametersRepository.findByTrsMidAndTerminalIdAndDeviceId(anyString(), anyString(), anyString())).thenReturn(emvParameters);
        when(inaPayMessages.get(anyString())).thenReturn("getResponse");

        ParamChecksumResponse result = getParamChecksumService.getParamChecksum(request);
        assertNotNull(result.getParamChecksum());
    }
    @Test
    void testGetParamChecksum_whenEmvParametersNotFound_thenThrow() {
        // Arrange
        GetParamChecksumRequest request=new GetParamChecksumRequest();
        request.setMid("mid");
        request.setTid("tid");
        request.setTrsMid("trsmid");
        request.setApiInContext(ApiInContext.builder()
                .timeStamp(String.valueOf(new Timestamp(System.currentTimeMillis())))
                .build());
        request.setDeviceMetadata(DeviceMetadata.builder()
                .deviceId("1234")
                .build());
        when(emvParametersRepository.findByTrsMidAndTerminalIdAndDeviceId(anyString(), anyString(), anyString())).thenReturn(null);

        when(inaPayMessages.get(CHECKSUM_NOT_FOUND)).thenReturn("Checksum not found");

        CommonValidationException exception = assertThrows(
                CommonValidationException.class,
                () -> getParamChecksumService.getParamChecksum(request));

        assertEquals(CHECKSUM_NOT_FOUND, exception.getCode());
    }
}

