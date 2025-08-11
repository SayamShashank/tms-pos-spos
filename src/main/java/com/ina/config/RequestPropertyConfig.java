package com.ina.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tms.request")
@Getter
@Setter
public class RequestPropertyConfig {
    private String krdSign2;
    private String shortName;
    private String trsMIdCounter;
    private String model;
    private String make;
    private String appVersion;
    private String tid;
    private String trsmId;
    private String serialNumber;
}
