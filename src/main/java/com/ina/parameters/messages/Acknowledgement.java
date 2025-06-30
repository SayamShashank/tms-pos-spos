package com.ina.parameters.messages;


import com.ina.config.RequestPropertyConfig;
import com.ina.parameters.utils.ParamUtils;
import com.ina.tms.packages.xml.v8.catm118.*;

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

        evtData.setTmStmp(ParamUtils.getCurrentDateTime());

        evtData.setRslt(TerminalManagementActionResult4Code.SUCC);

        TMSActionIdentification5 tmsAction = new TMSActionIdentification5();

        tmsAction.setActnTp(TerminalManagementAction4Code.DWNL);

        DataSetIdentification7 dataSetIdentification = new DataSetIdentification7();

        dataSetIdentification.setCreDtTm(ParamUtils.getCurrentDateTime());

        dataSetIdentification.setNm("Parameter"); 

        dataSetIdentification.setTp(DataSetCategory12Code.PARAMETERS);

        tmsAction.setDataSetId(dataSetIdentification);

        evtData.setActnId(tmsAction);
        
        return evtData;
    }
}
