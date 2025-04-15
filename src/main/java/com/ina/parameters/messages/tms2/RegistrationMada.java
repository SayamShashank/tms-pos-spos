package com.ina.parameters.messages.tms2;


import com.ina.config.RequestPropertyConfig;
import com.ina.parameters.messages.Registration;
import com.ina.parameters.packages.xml.v8.catm118.DataSetCategory12Code;
import com.ina.parameters.packages.xml.v8.catm118.Document;
import com.ina.parameters.utils.Marshal;

public class RegistrationMada extends Registration {

    public RegistrationMada(RequestPropertyConfig requestPropertyConfig) {
        super(requestPropertyConfig);
    }

    @Override
    public DataSetCategory12Code getCategoryCode() {
        return DataSetCategory12Code.REGISTER;
    }

//    public static void main(String[] args) {
//
//        RegistrationMada registration = new RegistrationMada();
//        Document doc = registration.genStsRpt(null);
//        Marshal marshal = new Marshal();
//        marshal.setUseCustomWriter(Boolean.TRUE);
//    }
}
