package com.ina.parameters.messages;

import com.ina.nexotms.packages.xml.v8.catm118.*;
import com.ina.parameters.model.ParameterRequestData;

import java.util.HashMap;


public interface NexoDataProviderInterface {

    //Header related values
    public String getInitiatingPartyId(String tid);
    public DataSetCategory12Code getCategoryCode();
    public String getPoiId(String id);
    public PartyType5Code getOriginatingPartyCode();
    public PartyType6Code getOriginatingIssuerCode();
    public PartyType5Code getRecipientPartyCode();
    public String getRecipientPartyId();
    public String getShortName(String name);

    //Status Report Related values 
    public PartyType5Code getPoiPartyCode();
    public PartyType6Code getPoiIssuerCode();

    public DataSetCategory12Code getDataSetCategoryCode();
    public DataSetCategory12Code getDataSetRequiredCode();
    public String getDataSetRequiredName();
    public String getDataSetRequiredVersion();

    HashMap<String, String> getAdditionalMgmtDetails(ParameterRequestData data);

    public TriggerInformation1 getInitTriggerDetails();

    public PointOfInteractionComponent9 getPoiComponent();

    

}
