package com.ina.parameters.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
public class SecUtils {

    private static final String SIGN_ALGO = "SHA256WithRSA";

    private SecUtils() {
    }

    private static class Holder {
        private static final SecUtils INSTANCE = new SecUtils();
    }

    public static SecUtils getInstance() {
        return Holder.INSTANCE;
    }

    public byte[] sign(byte[] data, KeyPair kdhSignKp) {
        return this.sign(data, kdhSignKp.getPrivate());
    }

    public byte[] sign(byte[] data, PrivateKey kdhSigSk) {
        try {
            Signature signature = Signature.getInstance(SIGN_ALGO);
            signature.initSign(kdhSigSk);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }





    byte[] readKeyFile(String fileName, Boolean isPk) {
        
        String keyHeader = "-----BEGIN PRIVATE KEY-----";
        String keyTrailer = "-----END PRIVATE KEY-----";
        String fileExt = ".key";
        if (isPk == Boolean.TRUE) {
            keyHeader = "-----BEGIN PUBLIC KEY-----";
            keyTrailer = "-----END PUBLIC KEY-----";
            fileExt = ".pem";
        }
        log.info("file:{}",fileName);
        // Read and process private key
        InputStream is = getClass().getResourceAsStream(fileName + fileExt);

        String keyContent = null;
        try {
            keyContent = new String(is.readAllBytes()).replace(keyHeader, "")
                    .replace(keyTrailer, "").replaceAll("\\s+", "");
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.getDecoder().decode(keyContent);
    }

    public PrivateKey getSk(String fileName) {
        byte[] keyBa = this.readKeyFile(fileName, Boolean.FALSE);
        PKCS8EncodedKeySpec skSpec = new PKCS8EncodedKeySpec(keyBa);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(skSpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

}
