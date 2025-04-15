package com.ina.parameters.messages;


import com.ina.config.RequestPropertyConfig;
import com.ina.parameters.packages.xml.v8.catm118.DataSetCategory12Code;

public class ParameterDownload extends BaseStatusReport{

    public ParameterDownload(RequestPropertyConfig requestPropertyConfig) {
        super(requestPropertyConfig);
    }
    @Override
    public DataSetCategory12Code getCategoryCode() {
        return DataSetCategory12Code.PARAMETERS;
    }
    @Override
    public DataSetCategory12Code getDataSetCategoryCode() {
        return DataSetCategory12Code.STATUS_REPORT;
    }
}
