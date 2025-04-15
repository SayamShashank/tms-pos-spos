package com.ina.parameters.config;
import lombok.Getter;
import lombok.Setter;

public class PoiConfig {

    @Getter @Setter
    String id;

    public PoiConfig() {
        //TODO: Initialize the values from DB
        id = "1";
    }
}