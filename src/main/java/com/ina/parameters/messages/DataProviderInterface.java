package com.ina.parameters.messages;

import com.ina.parameters.model.ParameterRequestData;
import com.ina.tms.packages.xml.v8.catm118.*;

import java.util.HashMap;


public interface DataProviderInterface {

    //Header related values
     String getInitiatingPartyId(String tid);
     DataSetCategory12Code getCategoryCode();
     String getPoiId(String id);
     PartyType5Code getOriginatingPartyCode();
     PartyType6Code getOriginatingIssuerCode();
     PartyType5Code getRecipientPartyCode();
     String getRecipientPartyId();
     String getShortName(String name);

    //Status Report Related values 
    PartyType5Code getPoiPartyCode();
    PartyType6Code getPoiIssuerCode();

    DataSetCategory12Code getDataSetCategoryCode();
    DataSetCategory12Code getDataSetRequiredCode();
    String getDataSetRequiredName();
    String getDataSetRequiredVersion();

    HashMap<String, String> getAdditionalMgmtDetails(ParameterRequestData data);

    TriggerInformation1 getInitTriggerDetails();

    PointOfInteractionComponent9 getPoiComponent();

    

}
