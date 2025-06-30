package com.ina.parameters.utils;

import lombok.extern.slf4j.Slf4j;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class ParamUtils {

    private static ParamUtils instance;
    private ParamUtils() {
    }

    public static ParamUtils getInstance() {
        if (instance == null) {
            instance = new ParamUtils();
        }
        return instance;
    }



    private static XMLGregorianCalendar toXMLGregorianCalendar(String dateString)
            throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(dateString);
    }

    public static XMLGregorianCalendar getCurrentDateTime() {
        try {
            // Get the current date and time in ISO 8601 format
            return toXMLGregorianCalendar(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }
    
}