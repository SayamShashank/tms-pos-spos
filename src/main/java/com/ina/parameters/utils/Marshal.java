package com.ina.parameters.utils;

import jakarta.xml.bind.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

@Getter // Generates getters for all fields
@Setter // Generates setters for all fields
@Slf4j
public class Marshal {

    private Boolean addNs;
    private Boolean useCustomWriter;

    private final String rejectNsUri = "urn:iso:std:iso:20022:tech:xsd:catm.004.001.04";
    private final String mgmtPlanNsuri = "urn:iso:std:iso:20022:tech:xsd:catm.002.001.07";
    private final String acptrConfigUpdateNsUri = "urn:iso:std:iso:20022:tech:xsd:catm.003.001.08";
    private String expectedNsUri;
    private static final Logger logger = LogManager.getLogger(Marshal.class);

    public <T> String marshalToXml(T obj, String rootName) {

        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());

            // Marshal the object using the custom writer
            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            // Create a StringWriter to capture the XML output
            StringWriter stringWriter = new StringWriter();
            // Create a XMLStreamWriter with the StringWriter
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = factory.createXMLStreamWriter(stringWriter);

            QName qName = null;
            if (addNs == Boolean.TRUE)
                qName = new QName("http://www.w3.org/2001/XMLSchema-instance", rootName);
            else
                qName = new QName(rootName);

            JAXBElement<T> root = new JAXBElement<>(qName, (Class<T>) obj.getClass(), obj);

            if (useCustomWriter == Boolean.TRUE) {
                // Wrap the writer with our custom namespace writer
                CustomNamespaceStreamWriter customWriter = new CustomNamespaceStreamWriter(writer);
                marshaller.marshal(root, customWriter);
            } else {
                marshaller.marshal(root, stringWriter);
            }
            return stringWriter.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T unmarshal(String xmlData, Class<T> classObj) {
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(classObj);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StreamSource source = new StreamSource(new StringReader(xmlData));
            JAXBElement<T> jaxbElement = unmarshaller.unmarshal(source, classObj);
            return jaxbElement.getValue();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

    // TODO: signature / MAC validation and other data validations
    public <T> T parseXml(String xmlData) {
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        fact.setNamespaceAware(true); // Set namespace awareness
        DocumentBuilder builder;
        try {
            builder = fact.newDocumentBuilder();
            org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(xmlData)));
            org.w3c.dom.Element rootElement = doc.getDocumentElement();
            this.setExpectedNsUri(rootElement.getNamespaceURI());
            if (expectedNsUri.equals(rejectNsUri)) {
                com.ina.parameters.packages.xml.v8.catm414.Document termMgmtRejDoc = this.unmarshal(xmlData, com.ina.parameters.packages.xml.v8.catm414.Document.class);
                log.info("reject reason: %s", termMgmtRejDoc.getTermnlMgmtRjctn().getRjct().getAddtlInf());
                return (T) termMgmtRejDoc;
            }
            else if (expectedNsUri.contains(mgmtPlanNsuri)) {
                com.ina.parameters.packages.xml.v8.catm217.Document mgmtPlanDoc = this.unmarshal(xmlData, com.ina.parameters.packages.xml.v8.catm217.Document.class);
                return (T) mgmtPlanDoc;
            }
            else if (expectedNsUri.contains(acptrConfigUpdateNsUri)) {
                com.ina.parameters.packages.xml.v8.catm318.Document acptrConfigUpdate = this.unmarshal(xmlData, com.ina.parameters.packages.xml.v8.catm318.Document.class);
                return (T) acptrConfigUpdate;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void m(String[] args) {

        Marshal marshal = new Marshal();
        //String rejectString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:catm.004.001.04\"><TermnlMgmtRjctn><Hdr><DwnldTrf>false</DwnldTrf><FrmtVrsn>8.0</FrmtVrsn><XchgId>323809000036</XchgId><CreDtTm>2023-08-27T14:26:28.375229</CreDtTm><InitgPty><Id>1142765102794458</Id><Tp>OriginatingPOI</Tp><Issr>MasterTerminalManager</Issr></InitgPty><RcptPty><Id>SPTMS2.0</Id><Tp>MasterTerminalManager</Tp></RcptPty></Hdr><Rjct><RjctRsn>MSGT</RjctRsn><AddtlInf>Terminal already Registered</AddtlInf></Rjct></TermnlMgmtRjctn></Document>";
        String mgmtPlanString = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:catm.002.001.07\"><MgmtPlanRplcmnt><Hdr><DwnldTrf>false</DwnldTrf><FrmtVrsn>8.0</FrmtVrsn><XchgId>300516000023</XchgId><CreDtTm>2023-01-05T16:31:44</CreDtTm><InitgPty><Id>1189264177080814</Id><Tp>OriginatingPOI</Tp><Issr>MasterTerminalManager</Issr></InitgPty><RcptPty><Id>SPTMS2.0</Id><Tp>MasterTerminalManager</Tp></RcptPty></Hdr><MgmtPlan><POIId><Id>1189264177080814</Id><Tp>OriginatingPOI</Tp><Issr>MasterTerminalManager</Issr></POIId><DataSet><Id><Tp>ManagementPlan</Tp><CreDtTm>2023-01-05T16:29:52.000+03:00</CreDtTm></Id><Cntt><Actn><Tp>Download</Tp><DataSetId><Nm>Terminal Registered Successfully</Nm><Tp>Parameters</Tp></DataSetId><Trggr>DATE</Trggr><TmCond><StartTm>2023-01-05T16:29:52.000+03:00</StartTm></TmCond></Actn><Actn><Tp>Download</Tp><DataSetId><Tp>ManagementPlan</Tp></DataSetId><Trggr>DATE</Trggr><TmCond><StartTm>2023-01-05T16:31:52.000+03:00</StartTm></TmCond></Actn></Cntt></DataSet></MgmtPlan><SctyTrlr><CnttTp>SIGN</CnttTp><SgndData><DgstAlgo><Algo>HS25</Algo></DgstAlgo><NcpsltdCntt><CnttTp>DATA</CnttTp></NcpsltdCntt><Sgnr><DgstAlgo><Algo>HS25</Algo></DgstAlgo><SgntrAlgo><Algo>ERS2</Algo></SgntrAlgo><Sgntr>NGUzYmY5NjJkYWM1MGVlOGIyYmJkMDkwOWY0ZDI2MzhlYmQ5MjE1Yzg3ZjQxNTc3MzIyNGVhYjgzMWEwYjhkYzJmMGZlMjMxZGMxNzBhM2M3ZWEwOTJjZTA5MGJjYTQyOTQzZDY0NjgzZTRmYzVkMjBmYzM0ZGZiNjA2Mzk2MDUwZWQxMjAxMDU4OGM5OTg2ZGRkNmI4NTlhNDAxZGI2YmUxN2MyZjNmZjA3ZDE5ZmNkNmMzMzU4ZmU2NzgwYmZlYTA3MWExODFmY2ViMDRmMmVjYTI4M2Q1NmE0MjllN2Y0YzQwZDE2OWY1OWI1MDExMGYzMTIxYmIyMTEzMGI2MWQ4ZjkzOTAzN2E2N2RmODhkY2NmMzdiMGNjZWM0MmY4MDc5MGEyNmY0ODM5M2JkMDk1ODc3OTMyYjA2ZDczMGRiZjk0MWQ3NjZmZWNlYWRkNGY1ZGJkYzM3MWZhYjRiN2U2MjMyODY1OGFjYzIxMmU5M2QwNmQ4MDljNWQ0MDc4Mzc0ZDkzMmJhYjQ4ZTdmYmYxN2FmZGUyMGFkNjIwODk4YTJiZjBjNTM2MGNiOGZkZTIyMmJiOTIyM2Q0ZmI3NDYzN2JiMTY5MThiMjg5OWRmZWY0YTBiYWU5MWU0OGYyOWNkYmQwMjA4NjM4NDMxY2JjY2MyN2Q2Zjk2NjQ0N2U=</Sgntr></Sgnr></SgndData></SctyTrlr></MgmtPlanRplcmnt></Document>";
        marshal.parseXml(mgmtPlanString);
    }
}