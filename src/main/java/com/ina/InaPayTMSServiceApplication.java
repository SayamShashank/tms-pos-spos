package com.ina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@ComponentScan({"com.ina"})
@EntityScan({"com.ina.common.dao.entity", "com.ina.transaction.entity","com.ina.common.crypto.entity","com.ina.dao.entity"})
@EnableJpaRepositories({"com.ina.common.dao", "com.ina.transaction.repository","com.ina.common.crypto.repository","com.ina.dao"})
@SpringBootApplication(exclude =  {DataSourceAutoConfiguration.class })
public class InaPayTMSServiceApplication {
    public static void main(String[] args) throws Exception {

        disableCertificateValidation();
        SpringApplication.run(InaPayTMSServiceApplication.class, args);
    }

    public static void disableCertificateValidation() throws Exception {
        // Create a trust manager that does not perform any certificate validation
        TrustManager[] trustAllCertificates = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // This method is intentionally left empty because it will be implemented in a subclass.
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // This method is intentionally left empty because it will be implemented in a subclass.
                    }
                }
        };

        // Set up the SSL context to use the "TrustAllCertificates" trust manager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCertificates, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}
