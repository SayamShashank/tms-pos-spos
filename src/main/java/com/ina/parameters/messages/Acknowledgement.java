package com.ina.parameters.messages;


import com.ina.config.RequestPropertyConfig;
import com.ina.nexotms.packages.xml.v8.catm118.*;
import com.ina.parameters.utils.NexoUtils;

public class Acknowledgement extends BaseStatusReport{

    public Acknowledgement(RequestPropertyConfig requestPropertyConfig) {
        super(requestPropertyConfig);
    }

    @Override
    public DataSetCategory12Code getCategoryCode() {
        return DataSetCategory12Code.MANAGEMENT_PLAN;
    }

    @Override
    public DataSetCategory12Code getDataSetCategoryCode() {
        return DataSetCategory12Code.STATUS_REPORT;
    }
    
    @Override
    TMSEvent6 createEvtData() {

        TMSEvent6 evtData = new TMSEvent6();

        evtData.setTmStmp(NexoUtils.getCurrentDateTime());

        evtData.setRslt(TerminalManagementActionResult4Code.SUCC);

        TMSActionIdentification5 tmsAction = new TMSActionIdentification5();

        tmsAction.setActnTp(TerminalManagementAction4Code.DWNL); //TODO: Acknowledgement based on response

        DataSetIdentification7 dataSetIdentification = new DataSetIdentification7();

        dataSetIdentification.setCreDtTm(NexoUtils.getCurrentDateTime());

        dataSetIdentification.setNm("Parameter"); 

        dataSetIdentification.setTp(DataSetCategory12Code.PARAMETERS);

        tmsAction.setDataSetId(dataSetIdentification);

        evtData.setActnId(tmsAction);
        
        return evtData;
    }
}
