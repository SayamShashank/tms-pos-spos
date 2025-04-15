package com.ina;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class InaPayTMSServiceApplicationTest {

    @BeforeEach
    void setUp() throws Exception {
        SSLContext defaultSSLContext = SSLContext.getDefault();
        HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLContext.getSocketFactory());
    }

    @Test
    void testDisableCertificateValidation() throws Exception {
        Method method = InaPayTMSServiceApplication.class.getDeclaredMethod("disableCertificateValidation");
        method.setAccessible(true);
        method.invoke(null);

        SSLSocketFactory socketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        assertNotNull(socketFactory, "SSLSocketFactory should not be null");
    }
}
