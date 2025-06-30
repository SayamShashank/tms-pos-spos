package com.ina.parameters.utils;

import jakarta.xml.bind.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

import static java.util.Objects.isNull;


@Getter
@Setter
@Slf4j
@Component
public class Marshal {

    private static final String REJECT_NS_URI = "urn:iso:std:iso:20022:tech:xsd:catm.004.001.04";
    private static final String MGMT_PLAN_NS_URI = "urn:iso:std:iso:20022:tech:xsd:catm.002.001.07";
    private static final String ACCEPT_CONFIG_UPDATE_NS_URI = "urn:iso:std:iso:20022:tech:xsd:catm.003.001.08";
    private static final String SCHEMA_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance";

    private Boolean addNs = false;
    private Boolean useCustomWriter = false;
    private String expectedNsUri;

    /**
     * Marshals an object to XML string.
     */
    public <T> String marshalToXml(T obj, String rootName) {
        if (obj == null || rootName == null || rootName.isEmpty()) {
            log.warn("marshalToXml: invalid arguments, obj={}, rootName={}", obj, rootName);
            return null;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            QName qName = Boolean.TRUE.equals(addNs)
                    ? new QName(SCHEMA_INSTANCE_NS, rootName)
                    : new QName(rootName);
            JAXBElement<T> rootElement = new JAXBElement<>(qName, (Class<T>) obj.getClass(), obj);

            StringWriter stringWriter = new StringWriter();
            if (Boolean.TRUE.equals(useCustomWriter)) {
                XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(stringWriter);
                CustomNamespaceStreamWriter customWriter = new CustomNamespaceStreamWriter(xmlWriter);
                marshaller.marshal(rootElement, customWriter);
            } else {
                marshaller.marshal(rootElement, stringWriter);
            }
            return stringWriter.toString();

        } catch (Exception e) {
            log.error("Failed to marshal object {}: {}", obj.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Unmarshals XML string to object.
     */
    public <T> T unmarshal(String xmlData, Class<T> targetClass) {
        if (xmlData == null || xmlData.isEmpty() || targetClass == null) {
            log.warn("unmarshal: invalid arguments, xmlData={}, targetClass={}", xmlData, targetClass);
            return null;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(targetClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StreamSource source = new StreamSource(new StringReader(xmlData));
            JAXBElement<T> jaxbElement = unmarshaller.unmarshal(source, targetClass);
            return jaxbElement.getValue();
        } catch (JAXBException e) {
            log.error("Failed to unmarshal XML to {}: {}", targetClass.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Parses XML, detects namespace, and deserializes to matching document type.
     */
    @SuppressWarnings("unchecked")
    public <T> T parseXml(String xmlData) {
        if (isNull(xmlData) || xmlData.isEmpty()) {
            log.warn("parseXml: empty XML data");
            return null;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(xmlData)));
            this.expectedNsUri = doc.getDocumentElement().getNamespaceURI();

            switch (expectedNsUri) {
                case REJECT_NS_URI -> {
                    com.ina.tms.packages.xml.v8.catm414.Document rejectDoc =
                            unmarshal(xmlData, com.ina.tms.packages.xml.v8.catm414.Document.class);
                    if (rejectDoc != null && rejectDoc.getTermnlMgmtRjctn() != null
                            && rejectDoc.getTermnlMgmtRjctn().getRjct() != null) {
                        log.info("Reject reason: {}", rejectDoc.getTermnlMgmtRjctn().getRjct().getAddtlInf());
                    }
                    return (T) rejectDoc;
                }
                case MGMT_PLAN_NS_URI -> {
                    return (T) unmarshal(xmlData, com.ina.tms.packages.xml.v8.catm217.Document.class);
                }
                case ACCEPT_CONFIG_UPDATE_NS_URI -> {
                    return (T) unmarshal(xmlData, com.ina.tms.packages.xml.v8.catm318.Document.class);
                }
                case null, default -> {
                    log.warn("Unknown namespace URI: {}", expectedNsUri);
                    return null;
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse XML: {}", e.getMessage(), e);
            return null;
        }
    }
}
