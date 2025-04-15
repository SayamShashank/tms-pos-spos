package com.ina.parameters.messages;


import com.ina.config.RequestPropertyConfig;
import com.ina.parameters.packages.xml.v8.catm118.DataSetCategory12Code;

public class SecurityParameterDownload extends BaseStatusReport{

    public SecurityParameterDownload(RequestPropertyConfig propertyConfig) {
        super(propertyConfig);
    }

    @Override
    public DataSetCategory12Code getCategoryCode() {
        return DataSetCategory12Code.SECURITY_PARAMETERS;
    }
}
