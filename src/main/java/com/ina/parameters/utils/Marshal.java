package com.ina.parameters.utils;

import jakarta.xml.bind.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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

    private static final String REJECT_NS_URI = "urn:iso:std:iso:20022:tech:xsd:catm.004.001.04";
    private static final String MGMT_PLAN_NS_URI = "urn:iso:std:iso:20022:tech:xsd:catm.002.001.07";
    private static final String ACCEPT_CONFIG_UPDATE_NS_URI = "urn:iso:std:iso:20022:tech:xsd:catm.003.001.08";
    private String expectedNsUri;


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
            if (expectedNsUri.equals(REJECT_NS_URI)) {
                com.ina.nexotms.packages.xml.v8.catm414.Document termMgmtRejDoc = this.unmarshal(xmlData, com.ina.nexotms.packages.xml.v8.catm414.Document.class);
                log.info("reject reason: %s", termMgmtRejDoc.getTermnlMgmtRjctn().getRjct().getAddtlInf());
                return (T) termMgmtRejDoc;
            }
            else if (expectedNsUri.contains(MGMT_PLAN_NS_URI)) {
                com.ina.nexotms.packages.xml.v8.catm217.Document mgmtPlanDoc = this.unmarshal(xmlData, com.ina.nexotms.packages.xml.v8.catm217.Document.class);
                return (T) mgmtPlanDoc;
            }
            else if (expectedNsUri.contains(ACCEPT_CONFIG_UPDATE_NS_URI)) {
                com.ina.nexotms.packages.xml.v8.catm318.Document acptrConfigUpdate = this.unmarshal(xmlData, com.ina.nexotms.packages.xml.v8.catm318.Document.class);
                return (T) acptrConfigUpdate;
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}