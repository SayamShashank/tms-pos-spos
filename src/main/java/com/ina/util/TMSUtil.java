package com.ina.util;

import com.ina.common.model.ApiOutContext;
import com.ina.common.response.message.InaPayMessages;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class TMSUtil {

    private final InaPayMessages messages;

    public TMSUtil(InaPayMessages messages) {
        this.messages = messages;
    }

    public static ApiOutContext getApiOutContext(String inputRefId, String code, String inaPayMessages) {
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(inputRefId);
        apiOutContext.setCode(code);
        apiOutContext.setMessage(inaPayMessages);
        apiOutContext.setTimeStamp(String.valueOf(LocalDateTime.now(ZoneId.systemDefault())));
        return apiOutContext;
    }

    public static ApiOutContext getApiOutContext(String inputRefId, String code, InaPayMessages inaPayMessages) {
        String msg = inaPayMessages.get(code);
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(inputRefId);
        apiOutContext.setCode(code);
        apiOutContext.setMessage(msg);
        apiOutContext.setTimeStamp(String.valueOf(LocalDateTime.now(ZoneId.systemDefault())));
        return apiOutContext;
    }

}
