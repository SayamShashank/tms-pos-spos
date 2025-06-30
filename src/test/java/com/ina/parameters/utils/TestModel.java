package com.ina.parameters.utils;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlRootElement(name = "TestRoot")
public class TestModel {
    private String field1;
    private int field2;
}
