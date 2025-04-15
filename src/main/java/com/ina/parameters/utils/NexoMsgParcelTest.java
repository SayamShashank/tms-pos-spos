//package com.ina.parameters.utils;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.ina.dao.EMVParametersRepository;
//import com.ina.dao.entity.EMVParameters;
//import com.ina.parameters.messages.Acknowledgement;
//import com.ina.parameters.messages.ParameterDownload;
//import com.ina.parameters.messages.Registration;
//import com.ina.parameters.model.TmsParams;
//import com.ina.parameters.packages.xml.v8.catm118.Document;
//import com.ina.parameters.packages.xml.v8.catm217.DataSetCategory12Code;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.codec.binary.Base64;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static com.ina.parameters.utils.JsonMapperUtil.getResult;
//
//
//@Getter // Generates getters for all fields
//@Setter // Generates setters for all fields
//@Slf4j
//public class NexoMsgParcelTest<T> {
//
//    private T respObj;
//    private final String rejectNsUri = "urn:iso:std:iso:20022:tech:xsd:catm.004.001.04";
//    private final String mgmtPlanNsuri = "urn:iso:std:iso:20022:tech:xsd:catm.002.001.07";
//    private final String acptrConfigUpdateNsUri = "urn:iso:std:iso:20022:tech:xsd:catm.003.001.08";
//    @Autowired
//    private EMVParametersRepository emvParametersRepository;
//
//
//    private <T> String exchange(T classObj) {
//
//        // 1. marshal to xml
//        Marshal marshal = new Marshal();
//        marshal.setUseCustomWriter(Boolean.FALSE);
//        marshal.setAddNs(Boolean.TRUE);
//        String stsRptMsg = marshal.marshalToXml(classObj, classObj.getClass().getSimpleName());
//        stsRptMsg = stsRptMsg.replaceAll("<xsi:Document", "<Document").replaceAll("</xsi:Document>", "</Document>");
//        log.info("req: ");
//        log.info(stsRptMsg);
//
//        // 2. send and receive
//        HttpClient httpClient = new HttpClient();
//        String ep = "statusReport.htm";
//        String respXmlData = httpClient.exchange(ep, stsRptMsg);
//        if (respXmlData != null) {
//
//            // 3. unmarshal and parse
//            log.info("resp: ");
//
//            log.info(respXmlData);
//            this.setRespObj(marshal.parseXml(respXmlData));
//            return marshal.getExpectedNsUri();
//        }
//        return null;
//    }
//
//    public Boolean testRegistration() {
//
//        Registration registration = new Registration();
//        Document doc = registration.genStsRpt(null);
//
//        log.info("registration: ");
//        // simulate successful resp flow
//        //respXmlData = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:catm.002.001.07\"><MgmtPlanRplcmnt><Hdr><DwnldTrf>false</DwnldTrf><FrmtVrsn>8.0</FrmtVrsn><XchgId>300516000023</XchgId><CreDtTm>2023-01-05T16:31:44</CreDtTm><InitgPty><Id>1189264177080814</Id><Tp>OriginatingPOI</Tp><Issr>MasterTerminalManager</Issr></InitgPty><RcptPty><Id>SPTMS2.0</Id><Tp>MasterTerminalManager</Tp></RcptPty></Hdr><MgmtPlan><POIId><Id>1189264177080814</Id><Tp>OriginatingPOI</Tp><Issr>MasterTerminalManager</Issr></POIId><DataSet><Id><Tp>ManagementPlan</Tp><CreDtTm>2023-01-05T16:29:52.000+03:00</CreDtTm></Id><Cntt><Actn><Tp>Download</Tp><DataSetId><Nm>Terminal Registered Successfully</Nm><Tp>Parameters</Tp></DataSetId><Trggr>DATE</Trggr><TmCond><StartTm>2023-01-05T16:29:52.000+03:00</StartTm></TmCond></Actn><Actn><Tp>Download</Tp><DataSetId><Tp>ManagementPlan</Tp></DataSetId><Trggr>DATE</Trggr><TmCond><StartTm>2023-01-05T16:31:52.000+03:00</StartTm></TmCond></Actn></Cntt></DataSet></MgmtPlan><SctyTrlr><CnttTp>SIGN</CnttTp><SgndData><DgstAlgo><Algo>HS25</Algo></DgstAlgo><NcpsltdCntt><CnttTp>DATA</CnttTp></NcpsltdCntt><Sgnr><DgstAlgo><Algo>HS25</Algo></DgstAlgo><SgntrAlgo><Algo>ERS2</Algo></SgntrAlgo><Sgntr>NGUzYmY5NjJkYWM1MGVlOGIyYmJkMDkwOWY0ZDI2MzhlYmQ5MjE1Yzg3ZjQxNTc3MzIyNGVhYjgzMWEwYjhkYzJmMGZlMjMxZGMxNzBhM2M3ZWEwOTJjZTA5MGJjYTQyOTQzZDY0NjgzZTRmYzVkMjBmYzM0ZGZiNjA2Mzk2MDUwZWQxMjAxMDU4OGM5OTg2ZGRkNmI4NTlhNDAxZGI2YmUxN2MyZjNmZjA3ZDE5ZmNkNmMzMzU4ZmU2NzgwYmZlYTA3MWExODFmY2ViMDRmMmVjYTI4M2Q1NmE0MjllN2Y0YzQwZDE2OWY1OWI1MDExMGYzMTIxYmIyMTEzMGI2MWQ4ZjkzOTAzN2E2N2RmODhkY2NmMzdiMGNjZWM0MmY4MDc5MGEyNmY0ODM5M2JkMDk1ODc3OTMyYjA2ZDczMGRiZjk0MWQ3NjZmZWNlYWRkNGY1ZGJkYzM3MWZhYjRiN2U2MjMyODY1OGFjYzIxMmU5M2QwNmQ4MDljNWQ0MDc4Mzc0ZDkzMmJhYjQ4ZTdmYmYxN2FmZGUyMGFkNjIwODk4YTJiZjBjNTM2MGNiOGZkZTIyMmJiOTIyM2Q0ZmI3NDYzN2JiMTY5MThiMjg5OWRmZWY0YTBiYWU5MWU0OGYyOWNkYmQwMjA4NjM4NDMxY2JjY2MyN2Q2Zjk2NjQ0N2U=</Sgntr></Sgnr></SgndData></SctyTrlr></MgmtPlanRplcmnt></Document>";
//        String expectedNsUri = this.exchange(doc);
//        if (expectedNsUri != null) {
//            if (expectedNsUri.equals(mgmtPlanNsuri)) {
//                com.ina.parameters.packages.xml.v8.catm217.Document mgmtPlanDoc = (com.ina.parameters.packages.xml.v8.catm217.Document) this
//                        .getRespObj();
//                        log.info("mgmt plan replace: ");
//                        log.info(mgmtPlanDoc.getMgmtPlanRplcmnt().getMgmtPlan().getDataSet()
//                        .getCntt().getActn().get(0).getDataSetId().getNm());
//                // check for content/Action/DataSetId/Type=Parameters
//                DataSetCategory12Code dsiType = mgmtPlanDoc.getMgmtPlanRplcmnt()
//                        .getMgmtPlan().getDataSet().getCntt().getActn().get(0).getDataSetId().getTp();
//                if (dsiType.value().equals("Parameters")) {
//                    return true;
//                }
//            }
//            else if (expectedNsUri.equals(rejectNsUri)) {
//                // TODO: processing for rejection
//            }
//            return true;
//        }
//        return false;
//    }
//
//    public Boolean testAcknowledgment() {
//
//        Acknowledgement acknowledgement = new Acknowledgement();
//        Document doc = acknowledgement.genStsRpt(null);
//
//        log.info("param dld ack: ");
//        String expectedNsUri = this.exchange(doc);
//        if (expectedNsUri != null) {
//
//            if (expectedNsUri.equals(mgmtPlanNsuri)) {
//                com.ina.parameters.packages.xml.v8.catm217.Document mgmtPlanDoc = (com.ina.parameters.packages.xml.v8.catm217.Document) this
//                        .getRespObj();
//                // TODO: check for Content/Action/DataSetId=ManagementPlan &
//                // Content/Action/DataSetId=SecurityParameters
//            }
//            return true;
//        }
//        return false;
//    }
//
//    public   Boolean testParamDownload() throws JsonProcessingException {
//
//        ParameterDownload paramDld = new ParameterDownload();
//        Document doc = paramDld.genStsRpt(null);
//
//        log.info("param dld: ");
//        String expectedNsUri = this.exchange(doc);
//        if (expectedNsUri != null) {
//            if (expectedNsUri.equals(acptrConfigUpdateNsUri)) {
//                com.ina.parameters.packages.xml.v8.catm318.Document acptrConfigUpdateDoc = (com.ina.parameters.packages.xml.v8.catm318.Document) this
//                        .getRespObj();
//                byte[] merchParams = acptrConfigUpdateDoc.getAccptrCfgtnUpd().getAccptrCfgtn().getDataSet().get(0)
//                        .getCntt().getMrchntParams().get(0).getOthrParams();
//                byte[] appParams =acptrConfigUpdateDoc.getAccptrCfgtnUpd().getAccptrCfgtn().getDataSet().get(0)
//                        .getCntt().getApplParams().get(0).getParams().get(0);
//                List<String> merchParamsBa = acptrConfigUpdateDoc.getAccptrCfgtnUpd()
//                        .getAccptrCfgtn()
//                        .getDataSet()
//                        .stream()
//                        .flatMap(dataset -> dataset.getCntt().getMrchntParams().stream()
//                                .map(param -> {
//
//                                    byte[] othrParams = param.getOthrParams();
//                                    return new String(Base64.decodeBase64(Base64.encodeBase64String(othrParams)));
//                                }))
//                        .toList();
//                List<String> appParamsList = acptrConfigUpdateDoc.getAccptrCfgtnUpd()
//                        .getAccptrCfgtn()
//                        .getDataSet()
//                        .stream()
//                        .flatMap(dataset -> dataset.getCntt()
//                                .getApplParams()
//                                .stream()
//                                .flatMap(applicationParams -> applicationParams.getParams()
//                                        .stream()
//                                        .map(param -> new String(Base64.decodeBase64(Base64.encodeBase64String(param))))))
//                        .collect(Collectors.toList());
//                JsonMapperUtil.Result result = getResult(appParamsList, merchParamsBa);
//                TmsParams tmsParams =new TmsParams();
//                tmsParams.setAids(result.aidLists());
//                tmsParams.setCpks(result.ridList());
//                tmsParams.setTerminalConfig(result.terminalConfig());
//                String params= result.objectMapper().writeValueAsString(tmsParams);
//                EMVParameters emvParameters =EMVParameters.builder()
//                        .deviceId("device-001")
//                        .merchantId(result.terminalConfig().getMerchantId())
//                        .terminalId(result.terminalConfig().getTerminalId())
//                        .aids(result.objectMapper().writeValueAsString(result.aidLists()))
//                        .cpks(result.objectMapper().writeValueAsString(result.ridList()))
//                        .terminalConfig(result.objectMapper().writeValueAsString(result.terminalConfig()))
//                        .build();
////                emvParametersRepository.save(emvParameters);
//                String emvParamString = result.objectMapper().writeValueAsString(emvParameters);
//
//                log.info("TMS params:{} ", params);
////                EMVParameters parameters=emvParametersRepository.findByDeviceId("device-001");
////                String parameterDownload = result.objectMapper().writeValueAsString(parameters);
////                log.info("Fetched Parameters:{}",parameterDownload);
//                // TODO: fetch all other parameters
//                // check for sending ack condition in the resp of param download
//            }
//            return true;
//        }
//        return false;
//    }
//
//    public static void main(String[] args) throws JsonProcessingException {
//
//        registerParamaterAck();
//    }
//
//    private static void registerParamaterAck() throws JsonProcessingException {
//        NexoMsgParcelTest nmpTest = new NexoMsgParcelTest<>();
//
//        if (nmpTest.testRegistration() == Boolean.TRUE) {
//            if (nmpTest.testParamDownload() == Boolean.TRUE) {
//                nmpTest.testAcknowledgment();
//            }
//            }
//    }
//}
//
