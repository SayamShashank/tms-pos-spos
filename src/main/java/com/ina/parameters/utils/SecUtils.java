package com.ina.parameters.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.*;
import java.util.Base64;
import java.util.Scanner;

@Slf4j
public class SecUtils {

    private static SecUtils instance;
    private static final String signAlgo = "SHA256WithRSA";

    public static SecUtils getInstance() {
        if (instance == null)
            instance = new SecUtils();
        return instance;
    }

    KeyPair createKey(String modulus, String pubExp, String privExp, String primeP, String primeQ,
            String expP, String expQ, String coEff) {

        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(
                new BigInteger(modulus, 16), new BigInteger(pubExp, 16));

        RSAPrivateCrtKeySpec privKeySpec = new RSAPrivateCrtKeySpec(
                new BigInteger(modulus, 16), new BigInteger(pubExp, 16),
                new BigInteger(privExp, 16), new BigInteger(primeP, 16),
                new BigInteger(primeQ, 16), new BigInteger(expP, 16),
                new BigInteger(expQ, 16), new BigInteger(coEff, 16));

        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            KeyPair keyPair = new KeyPair(kf.generatePublic(pubKeySpec), kf.generatePrivate(privKeySpec));
            return keyPair;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] sign(byte[] data, KeyPair kdhSignKp) {
        return this.sign(data, kdhSignKp.getPrivate());
    }

    public byte[] sign(byte[] data, PrivateKey kdhSigSk) {
        try {
            Signature signature = Signature.getInstance(signAlgo);
            signature.initSign(kdhSigSk);
            signature.update(data);
            byte[] baSign = signature.sign();
            return baSign;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] sha256(byte[] input) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String readCert(String pemFilePath) {
        // Read the PEM file content
        StringBuilder pemContent = new StringBuilder();
        try (InputStream inputStream = getClass().getResourceAsStream(pemFilePath);
            Scanner scanner = new Scanner(inputStream)) {

            boolean inPemBlock = false;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.startsWith("-----BEGIN CERTIFICATE-----")) {
                    inPemBlock = true;
                } else if (line.startsWith("-----END CERTIFICATE-----")) {
                    inPemBlock = false;
                } else if (inPemBlock) {
                    pemContent.append(line);
                }
            } 
            return pemContent.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private X509Certificate genCert(String pemFilePath) {
        String pemContent = this.readCert(pemFilePath);
        // Decode the PEM content and create an X509Certificate
        byte[] certBytes = Base64.getDecoder().decode(pemContent);
        CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory
                    .generateCertificate(new ByteArrayInputStream(certBytes));
        } 
        catch (CertificateException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PublicKey getPkfromCert(String pemFilePath) {
        return this.genCert(pemFilePath).getPublicKey();   
    }

    public String getSha1TpfromCert(String pemFilePath) {
        String pemContent = this.readCert(pemFilePath);
        // Decode the PEM content and create an X509Certificate
        byte[] certBytes = Base64.getDecoder().decode(pemContent);

        // Calculate the SHA-1 thumbprint
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            byte[] thumbprintBytes = md.digest(certBytes);
            StringBuilder thumbprintHex = new StringBuilder();
            for (byte b : thumbprintBytes) {
                thumbprintHex.append(String.format("%02X", b));
            }
            return thumbprintHex.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
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

    public PublicKey getPk(String fileName) {
        byte[] keyBa = this.readKeyFile(fileName, Boolean.TRUE);
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(keyBa);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(pkSpec);
        } 
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
