package com.ina.parameters.utils;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.ina.constants.AppConstants.*;

public class CustomNamespaceStreamWriter implements XMLStreamWriter {
    private XMLStreamWriter delegate;

    public CustomNamespaceStreamWriter(XMLStreamWriter delegate) {
        this.delegate = delegate;
    }

    // Override methods as needed. For example:
    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        if (!"urn:iso:std:iso:20022:tech:xsd:catm.001.001.08".equals(namespaceURI)) {
            delegate.writeNamespace(prefix, namespaceURI); // handle other namespaces normally
        }
    }

    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        delegate.writeStartElement(localName);
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        if (NAME_SPACE.equals(namespaceURI)) {
            delegate.writeStartElement(localName); // use default namespace
        } else {
            delegate.writeStartElement(namespaceURI, localName);
        }
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        if (NAME_SPACE.equals(namespaceURI)) {
            delegate.writeStartElement("", localName, namespaceURI);  // start element in default namespace
        } else {
            delegate.writeStartElement(prefix, localName, namespaceURI);  // handle other elements normally
        }
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(UNIMPLEMENTED);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(UNIMPLEMENTED);
    }

    @Override
    public void writeEmptyElement(String localName)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(UNIMPLEMENTED);
    }

    @Override
    public void writeEndElement() throws XMLStreamException {
        delegate.writeEndElement();
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        delegate.writeEndDocument();
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'close'");
    }

    @Override
    public void flush() throws XMLStreamException {
        delegate.flush();
    }

    @Override
    public void writeAttribute(String localName, String value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(UNIMPLEMENTED_ATT);
    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
            {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(UNIMPLEMENTED_ATT);
    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(UNIMPLEMENTED_ATT);
    }


    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        if (!NAME_SPACE.equals(namespaceURI)) {
            delegate.writeDefaultNamespace(namespaceURI);
        }
    }

    @Override
    public void writeComment(String data)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeComment'");
    }

    @Override
    public void writeProcessingInstruction(String target)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeProcessingInstruction'");
    }

    @Override
    public void writeProcessingInstruction(String target, String data)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeProcessingInstruction'");
    }

    @Override
    public void writeCData(String data)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeCData'");
    }

    @Override
    public void writeDTD(String dtd)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeDTD'");
    }

    @Override
    public void writeEntityRef(String name)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeEntityRef'");
    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        delegate.writeStartDocument();
    }

    @Override
    public void writeStartDocument(String version)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeStartDocument'");
    }

    @Override
    public void writeStartDocument(String encoding, String version)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeStartDocument'");
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        delegate.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        delegate.writeCharacters(text, start, len);
    }

    @Override
    public String getPrefix(String uri)  {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPrefix'");
    }

    @Override
    public void setPrefix(String prefix, String uri) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setPrefix'");
    }

    @Override
    public void setDefaultNamespace(String uri) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDefaultNamespace'");
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setNamespaceContext'");
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return delegate.getNamespaceContext();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getProperty'");
    }
}
