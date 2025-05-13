package com.ina.parameters.messages.tms2;


import com.ina.config.RequestPropertyConfig;
import com.ina.nexotms.packages.xml.v8.catm118.DataSetCategory12Code;
import com.ina.parameters.messages.Registration;


public class RegistrationMada extends Registration {

    public RegistrationMada(RequestPropertyConfig requestPropertyConfig) {
        super(requestPropertyConfig);
    }

    @Override
    public DataSetCategory12Code getCategoryCode() {
        return DataSetCategory12Code.REGISTER;
    }


}
