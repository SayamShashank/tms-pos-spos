package com.ina.parameters.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class MarshalTest {

    private Marshal marshal;

    @BeforeEach
    void setUp() {
        marshal = new Marshal();
        marshal.setAddNs(false);
        marshal.setUseCustomWriter(false);
    }

    @Test
    void testMarshalToXml_withoutNamespace() {
        TestModel model = new TestModel();
        model.setField1("Hello");
        model.setField2(123);

        String xml = marshal.marshalToXml(model, "TestRoot");

        assertNotNull(xml);
        assertTrue(xml.contains("<TestRoot"));
        assertTrue(xml.contains("Hello"));
        assertTrue(xml.contains("123"));
    }

    @Test
    void testMarshalToXml_withNamespace() {
        marshal.setAddNs(true);
        TestModel model = new TestModel();
        model.setField1("WithNs");
        model.setField2(456);

        String xml = marshal.marshalToXml(model, "TestRoot");

        assertNotNull(xml);
        assertTrue(xml.contains("TestRoot"));
        assertTrue(xml.contains("WithNs"));
    }

    @Test
    void testUnmarshal() {
        String xml = "<TestRoot><field1>Value1</field1><field2>42</field2></TestRoot>";

        TestModel model = marshal.unmarshal(xml, TestModel.class);

        assertNotNull(model);
        assertEquals("Value1", model.getField1());
        assertEquals(42, model.getField2());
    }

    @Test
    void testParseXml_rejectNamespace() {
        // sample XML with reject namespace
        String xml = "<?xml version=\"1.0\"?>" +
                "<Document xmlns=\"" +  "urn:iso:std:iso:20022:tech:xsd:catm.004.001.04" + "\">" +
                "<TermnlMgmtRjctn><Rjct><AddtlInf>Rejected</AddtlInf></Rjct></TermnlMgmtRjctn>" +
                "</Document>";

        marshal.setExpectedNsUri( "urn:iso:std:iso:20022:tech:xsd:catm.004.001.04");

        // Assuming parseXml returns Document object, cast accordingly
        Object parsed = marshal.parseXml(xml);

        assertNotNull(parsed);
    }

    @Test
    void testParseXml_unknownNamespace_returnsNull() {
        String xml = "<root xmlns=\"unknown:namespace\"></root>";

        Object parsed = marshal.parseXml(xml);

        assertNull(parsed);
    }
}
