package com.ina.parameters.utils;

import lombok.extern.slf4j.Slf4j;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Slf4j
public class NexoUtils {

    private static NexoUtils instance;
    private final Properties prop = new Properties();
    private String propFilePath;
    private String propFileName;

    public static NexoUtils getInstance() {
        if (instance == null) {
            instance = new NexoUtils();
        }
        return instance;
    }

    private NexoUtils() {
        InputStream in;
        propFileName = "/application.properties";

        try {
            in = this.getClass().getResourceAsStream(propFileName);
            prop.load(in);
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getProp(String key) {
        return prop.getProperty(key);
    }

    public void setProp(String key, String value) {
        prop.setProperty(key, value);
        flush();
    }

    public void flush() {
        try (OutputStream out = new FileOutputStream(propFilePath + propFileName)) {
            prop.store(out, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public BigDecimal getExchangeId() {
        String xchdIdStr = getProp("xchgid");
        BigDecimal xchgId = new BigDecimal(xchdIdStr);
        xchgId = xchgId.add(BigDecimal.ONE);
        xchdIdStr = String.format("%012d", xchgId.longValue()); // Convert to long
        log.info("xchgid:{}",xchdIdStr);
        setProp("xchgid", xchdIdStr);

        return new BigDecimal(xchdIdStr);
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