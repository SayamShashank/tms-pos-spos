package com.ina.util;

import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiOutContext;
import com.ina.common.response.message.InaPayMessages;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class TMSUtil {



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
    public static CommonValidationException throwValidationException(String inputRefId, String code, InaPayMessages inaPayMessages) {
        String message=inaPayMessages.get(code,"");
        return new CommonValidationException(
                inputRefId,
                code,
                message,
                null
        );
    }
    public static String generateUniqueId() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
        return formatter.format(new Date());
    }

}
