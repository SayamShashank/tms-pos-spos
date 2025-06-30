package com.ina.parameters.utils;

import org.junit.jupiter.api.Test;

import javax.xml.datatype.XMLGregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

class ParamUtilsTest {

    @Test
    void testSingletonInstance() {
        ParamUtils instance1 = ParamUtils.getInstance();
        ParamUtils instance2 = ParamUtils.getInstance();

        assertNotNull(instance1, "Instance should not be null");
        assertSame(instance1, instance2, "Both instances should be the same (singleton)");
    }

    @Test
    void testGetCurrentDateTime() {
        XMLGregorianCalendar currentDateTime = ParamUtils.getCurrentDateTime();

        assertNotNull(currentDateTime, "Current date time should not be null");

        String xmlDateTime = currentDateTime.toXMLFormat();
        assertNotNull(xmlDateTime, "XML format of date time should not be null");
        assertFalse(xmlDateTime.isEmpty(), "XML format string should not be empty");

        // Optionally, check that the year matches current year
        int currentYear = java.time.LocalDate.now().getYear();
        assertEquals(currentYear, currentDateTime.getYear(), "Year should match the current year");
    }
}
