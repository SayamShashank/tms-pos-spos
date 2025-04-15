package com.ina.parameters.messages;

import com.ina.config.RequestPropertyConfig;
import com.ina.parameters.model.ParameterRequestData;
import com.ina.parameters.packages.xml.v8.catm118.DataSetCategory12Code;
import com.ina.parameters.packages.xml.v8.catm118.Document;
import com.ina.parameters.utils.HttpClient;
import com.ina.parameters.utils.Marshal;

import java.util.HashMap;



public class Registration extends BaseStatusReport{

    public Registration(RequestPropertyConfig requestPropertyConfig) {
        super(requestPropertyConfig);
    }

    @Override
    public DataSetCategory12Code getCategoryCode() {
        return DataSetCategory12Code.REGISTER;
    }

    @Override
    public DataSetCategory12Code getDataSetCategoryCode() {
        return DataSetCategory12Code.STATUS_REPORT;
    }

    @Override
    public HashMap<String, String> getAdditionalMgmtDetails(ParameterRequestData data) {
        
        HashMap<String, String> additionalMgmtDetails =  super.getAdditionalMgmtDetails(data);

        additionalMgmtDetails.put("Register", "R");

        return additionalMgmtDetails;
    }


}

