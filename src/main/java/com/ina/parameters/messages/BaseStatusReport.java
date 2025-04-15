package com.ina.parameters.messages;

import com.ina.config.RequestPropertyConfig;
import com.ina.parameters.model.ParameterRequestData;
import com.ina.parameters.packages.xml.v8.catm118.*;
import com.ina.parameters.utils.Marshal;
import com.ina.parameters.utils.NexoUtils;
import com.ina.parameters.utils.SecUtils;
import com.ina.util.TMSUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


public class BaseStatusReport implements NexoDataProviderInterface {
    private  final RequestPropertyConfig propertyConfig;

    DataSetCategory12Code category12Code = DataSetCategory12Code.STRP;
    NexoDataProviderInterface dataProviderInterface = null;

    public BaseStatusReport(RequestPropertyConfig propertyConfig) {
        this.propertyConfig = propertyConfig;
        this.dataProviderInterface = this;
    }

    GenericIdentification71 setInitgPty(ParameterRequestData data) {

        GenericIdentification71 initgPty = new GenericIdentification71();
        // 5.1. id
        initgPty.setId(getInitiatingPartyId(data.getTid()));
        // 5.2. type
        initgPty.setTp(getOriginatingPartyCode());
        // 5.3. issuer
        initgPty.setIssr(getOriginatingIssuerCode());
        // 5.4. short name
        initgPty.setShrtNm(getShortName(propertyConfig.getShortName()));
        return initgPty;
    }

    GenericIdentification92 setRcptPty() {

        GenericIdentification92 rcptPty = new GenericIdentification92();
        // 6.1. id
        rcptPty.setId("SPTMS2.0"); // TODO: assign from prop
        // 6.2. type
        rcptPty.setTp(getRecipientPartyCode());

        return rcptPty;
    }

    Header27 createHdr(ParameterRequestData data) {

        Header27 header = new Header27();

        // 1. download transfer
        header.setDwnldTrf(false);
        // 2. firmware version
        header.setFrmtVrsn("8.0");
        // 3. exchange id
        header.setXchgId(new BigDecimal(TMSUtil.generateUniqueId()));
        // 4. creation date and time
        header.setCreDtTm(NexoUtils.getCurrentDateTime());
        // 5. initiating party
        header.setInitgPty(this.setInitgPty(data));
        // 6. receiving party
        header.setRcptPty(this.setRcptPty());

        return header;
    }

    ContentInformationType18 createSctyTrlr(byte[] inSignature) {

        ContentInformationType18 sctyTrlr = new ContentInformationType18();

        // 1. content type
        sctyTrlr.setCnttTp(ContentType2Code.SIGN); // TODO: Initial stat report untill symmetric keys are not exchanged
                                                   // will sign otherwise will use MAC
        // 2. signed data
        SignedData5 signedData = new SignedData5();
        // 2.1. digest algo
        AlgorithmIdentification21 algoId = new AlgorithmIdentification21();
        algoId.setAlgo(Algorithm16Code.HS_25);
        signedData.getDgstAlgo().add(algoId);
        // 2.2. ncpsltdCntt
        EncapsulatedContent3 ncpsltdCntt = new EncapsulatedContent3();
        ncpsltdCntt.setCnttTp(ContentType2Code.DATA);
        signedData.setNcpsltdCntt(ncpsltdCntt);
        // 2.3. signer
        Signer4 signer = new Signer4();
        // 2.3.1. digest algo
        signer.setDgstAlgo(algoId);
        // 2.3.2. signature algo
        AlgorithmIdentification20 signtrAlgoId = new AlgorithmIdentification20();
        signtrAlgoId.setAlgo(Algorithm19Code.ERS_2);
        signer.setSgntrAlgo(signtrAlgoId);
        // 2.3.3. signature
        signer.setSgntr(inSignature);
        signedData.getSgnr().add(signer);

        sctyTrlr.setSgndData(signedData);
        return sctyTrlr;
    }

    StatusReport8 createStsRpt(ParameterRequestData data) {

        StatusReport8 stsRpt = new StatusReport8();
        // 1. poi id
        GenericIdentification71 poiId = new GenericIdentification71();
        // 1.1. id
        poiId.setId(getPoiId(data.getTid()));
        // 1.2. type
        poiId.setTp(getPoiPartyCode());
        // 1.3. issuer
        poiId.setIssr(getPoiIssuerCode());

        stsRpt.setPOIId(poiId);

        // 2. data set
        TerminalManagementDataSet28 dataSet = new TerminalManagementDataSet28();
        // 2.1. id
        DataSetIdentification7 id = new DataSetIdentification7();
        // 2.1.1. type
        id.setTp(getDataSetCategoryCode());
        // 2.1.2. create date and time
        id.setCreDtTm(NexoUtils.getCurrentDateTime());
        dataSet.setId(id);

        // 2.2. content
        StatusReportContent8 cntt = new StatusReportContent8();
        // 2.2.1. POI date and time
        cntt.setPOIDtTm(NexoUtils.getCurrentDateTime());
        // 2.2.2. data set required
        TerminalManagementDataSet25 dataSetReqrd = new TerminalManagementDataSet25();
        // 2.2.2.1. dsr id
        DataSetIdentification7 dsrId = new DataSetIdentification7();
        // 2.2.2.1.1. dsrid type
        dsrId.setTp(getDataSetRequiredCode());
        if (getDataSetRequiredName() != null)
            dsrId.setNm(getDataSetRequiredName());

        if (getDataSetRequiredVersion() != null) {
            dsrId.setVrsn(getDataSetRequiredVersion());
            dsrId.setCreDtTm(NexoUtils.getCurrentDateTime());
        }

        dataSetReqrd.setId(dsrId);

        // TODO: Looks xsd converts data to base64, TMS2 expects ascii (some bytes)
        if (getPoiChallenge() != null)
            dataSetReqrd.setPOIChllng(getPoiChallenge());

        // TODO: Looks xsd converts data to base64, TMS2 expects ascii (some bytes)
        if (getPoiChallenge() != null)
            dataSetReqrd.setTMChllng(getTmChallenge());

        // 2.2.2.2. ssn key
        CryptographicKey13 ssnKey = new CryptographicKey13();

        HashMap<String, String> additionalMgmtDetails = getAdditionalMgmtDetails(data);

        // 2.2.2.2.1. additional mgmt info
        for (Map.Entry<String, String> entry : additionalMgmtDetails.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            GenericInformation1 addtlMgmtInf = new GenericInformation1();
            addtlMgmtInf.setNm(key);
            addtlMgmtInf.setVal(value);
            ssnKey.getAddtlMgmtInf().add(addtlMgmtInf);
        }

        dataSetReqrd.setSsnKey(ssnKey);
        cntt.getDataSetReqrd().add(dataSetReqrd);

        if (getAttendedContext() != null)
            cntt.setAttndncCntxt(getAttendedContext());

        // Events Data
        TMSEvent6 tmsEvent = this.createEvtData();
        if (tmsEvent != null) {
            cntt.getEvt().add(tmsEvent);
        }

        PointOfInteractionComponent9 poiComponent = getPoiComponent();
        if (poiComponent != null) {
            cntt.getPOICmpnt().add(poiComponent);
        }

        dataSet.setCntt(cntt);

        TriggerInformation1 triggerInformation = getInitTriggerDetails();
        if (triggerInformation != null)
            stsRpt.setInitgTrggr(getInitTriggerDetails());

        stsRpt.setDataSet(dataSet);

        return stsRpt;
    }

    byte[] createSignature(byte[] msg) {
        // RkiKeys rkiKeys = new RkiKeys();
        return SecUtils.getInstance().sign(msg, SecUtils.getInstance().getSk("/keys/"+propertyConfig.getKrdSign2()));
    }

    public Document genStsRpt(ParameterRequestData data) {

        StatusReportV08 stsRpt = new StatusReportV08();
        stsRpt.setHdr(this.createHdr(data));
        StatusReport8 stsRpt8Obj = this.createStsRpt(data);
        stsRpt.setStsRpt(stsRpt8Obj);

        Marshal marshal = new Marshal();
        marshal.setUseCustomWriter(Boolean.TRUE);
        String msgBody = marshal.marshalToXml(stsRpt8Obj, "StsRpt");

        byte[] signature = this.createSignature(msgBody.getBytes());
        stsRpt.setSctyTrlr(this.createSctyTrlr(signature));

        Document document = new Document();
        document.setStsRpt(stsRpt);
        return document;
    }

    public String getTmChallenge() {
        return null;
    }

    public String getPoiChallenge() {
        return null;
    }

    public AttendanceContext1Code getAttendedContext() {
        return null;
    }

    public PointOfInteractionComponent9 getPoiComponent() {
        return null;
    }

    public TriggerInformation1 getInitTriggerDetails() {
        return null;
    }

    TMSEvent6 createEvtData() {
        return null;
    }

    @Override
    public DataSetCategory12Code getCategoryCode() {
        return DataSetCategory12Code.REGISTER;
    }

    @Override
    public String getPoiId(String id) {
        return id;
    }

    @Override
    public PartyType5Code getOriginatingPartyCode() {
        return PartyType5Code.ORIGINATING_POI;
    }

    @Override
    public PartyType6Code getOriginatingIssuerCode() {
        return PartyType6Code.MASTER_TERMINAL_MANAGER;
    }

    @Override
    public String getShortName(String shortName) {
        return shortName;
    }

    @Override
    public String getInitiatingPartyId(String tid) {
        return tid;
    }

    @Override
    public PartyType5Code getRecipientPartyCode() {
        return PartyType5Code.MASTER_TERMINAL_MANAGER;
    }

    @Override
    public String getRecipientPartyId() {
        return "1";
    }

    @Override
    public PartyType5Code getPoiPartyCode() {
        return PartyType5Code.ORIGINATING_POI;
    }

    @Override
    public PartyType6Code getPoiIssuerCode() {
        return PartyType6Code.MASTER_TERMINAL_MANAGER;
    }

    @Override
    public DataSetCategory12Code getDataSetCategoryCode() {
        return DataSetCategory12Code.PARA;
    }

    @Override
    public DataSetCategory12Code getDataSetRequiredCode() {
        return getCategoryCode();
    }

    public HashMap<String, String> getAdditionalMgmtDetails(ParameterRequestData data) {

        HashMap<String, String> additionalMgmtDetails = new HashMap<>();

        additionalMgmtDetails.put("make", propertyConfig.getMake());

        additionalMgmtDetails.put("model", propertyConfig.getModel());

        additionalMgmtDetails.put("trsmId", data.getTrsMid());

        additionalMgmtDetails.put("serialNo", data.getSerialNo());

        additionalMgmtDetails.put("appVersion", propertyConfig.getAppVersion());

        additionalMgmtDetails.put("trsmIdCounter", propertyConfig.getTrsMIdCounter());

        additionalMgmtDetails.put("keySerialNo", data.getKeySerialNo());

        return additionalMgmtDetails;

    }

    @Override
    public String getDataSetRequiredName() {
        return null;
    }

    @Override
    public String getDataSetRequiredVersion() {
        return null;
    }
}
