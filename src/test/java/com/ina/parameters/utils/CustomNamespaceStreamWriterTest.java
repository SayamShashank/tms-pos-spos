package com.ina.parameters.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.ina.constants.AppConstants.NAME_SPACE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CustomNamespaceStreamWriterTest {

    private XMLStreamWriter mockWriter;
    private CustomNamespaceStreamWriter writer;

    @BeforeEach
    void setUp() {
        mockWriter = mock(XMLStreamWriter.class);
        writer = new CustomNamespaceStreamWriter(mockWriter);
    }

    @Test
    void testWriteNamespace_shouldDelegateForOtherNamespaces() throws XMLStreamException {
        String prefix = "xsi";
        String otherNamespace = "http://example.com";
        writer.writeNamespace(prefix, otherNamespace);
        verify(mockWriter).writeNamespace(prefix, otherNamespace);
    }

    @Test
    void testWriteNamespace_shouldSkipForSpecificNamespace() throws XMLStreamException {
        String prefix = "xsi";
        String blockedNamespace = "urn:iso:std:iso:20022:tech:xsd:catm.001.001.08";
        writer.writeNamespace(prefix, blockedNamespace);
        verify(mockWriter, never()).writeNamespace(anyString(), eq(blockedNamespace));
    }

    @Test
    void testWriteStartElement_withNamespaceURI_shouldUseDefaultNamespace() throws XMLStreamException {
        String localName = "element";
        writer.writeStartElement(NAME_SPACE, localName);
        verify(mockWriter).writeStartElement(localName);
    }

    @Test
    void testWriteStartElement_withNamespaceURI_shouldDelegateNormally() throws XMLStreamException {
        String localName = "element";
        String otherNamespace = "http://example.com";
        writer.writeStartElement(otherNamespace, localName);
        verify(mockWriter).writeStartElement(otherNamespace, localName);
    }

    @Test
    void testWriteStartElement_withPrefix_shouldUseDefaultNamespace() throws XMLStreamException {
        String prefix = "pfx";
        String localName = "element";
        writer.writeStartElement(prefix, localName, NAME_SPACE);
        verify(mockWriter).writeStartElement("", localName, NAME_SPACE);
    }

    @Test
    void testWriteStartElement_withPrefix_shouldDelegateNormally() throws XMLStreamException {
        String prefix = "pfx";
        String localName = "element";
        String otherNamespace = "http://example.com";
        writer.writeStartElement(prefix, localName, otherNamespace);
        verify(mockWriter).writeStartElement(prefix, localName, otherNamespace);
    }

    @Test
    void testWriteDefaultNamespace_shouldSkipForSpecificNamespace() throws XMLStreamException {
        writer.writeDefaultNamespace(NAME_SPACE);
        verify(mockWriter, never()).writeDefaultNamespace(NAME_SPACE);
    }

    @Test
    void testWriteDefaultNamespace_shouldDelegateForOtherNamespace() throws XMLStreamException {
        String otherNamespace = "http://example.com";
        writer.writeDefaultNamespace(otherNamespace);
        verify(mockWriter).writeDefaultNamespace(otherNamespace);
    }

    @Test
    void testWriteEndElement_shouldDelegate() throws XMLStreamException {
        writer.writeEndElement();
        verify(mockWriter).writeEndElement();
    }

    @Test
    void testWriteEndDocument_shouldDelegate() throws XMLStreamException {
        writer.writeEndDocument();
        verify(mockWriter).writeEndDocument();
    }

    @Test
    void testFlush_shouldDelegate() throws XMLStreamException {
        writer.flush();
        verify(mockWriter).flush();
    }

    @Test
    void testWriteCharacters_shouldDelegate() throws XMLStreamException {
        String text = "test";
        writer.writeCharacters(text);
        verify(mockWriter).writeCharacters(text);
    }

    @Test
    void testWriteCharacters_withArray_shouldDelegate() throws XMLStreamException {
        char[] text = {'a', 'b', 'c'};
        writer.writeCharacters(text, 0, 3);
        verify(mockWriter).writeCharacters(text, 0, 3);
    }

    @Test
    void testUnsupportedMethods_shouldThrowException() {
        assertAll(
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeEmptyElement("ns", "ln")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeEmptyElement("prefix", "ln", "ns")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeEmptyElement("ln")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.close()),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeAttribute("ln", "val")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeAttribute("prefix", "ns", "ln", "val")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeAttribute("ns", "ln", "val")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeComment("comment")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeProcessingInstruction("target")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeProcessingInstruction("target", "data")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeCData("data")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeDTD("dtd")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeEntityRef("ref")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeStartDocument("1.0")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.writeStartDocument("UTF-8", "1.0")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.getPrefix("uri")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.setPrefix("pfx", "uri")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.setDefaultNamespace("uri")),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.setNamespaceContext(null)),
                () -> assertThrows(UnsupportedOperationException.class, () -> writer.getProperty("name"))
        );
    }
}
