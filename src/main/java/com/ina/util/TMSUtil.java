package com.ina.util;

import com.ina.common.exception.CommonValidationException;
import com.ina.common.response.message.InaPayMessages;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class TMSUtil {
    private TMSUtil() {
    }

    public static String generateUniqueId() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss");
        return formatter.format(new Date());
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

}
