package com.ina.util;

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

}
