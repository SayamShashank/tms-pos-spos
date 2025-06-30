package com.ina.parameters.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SecUtilsTest {

    private static SecUtils secUtils;

    @BeforeAll
    static void init() {
        secUtils = SecUtils.getInstance();
    }

    @Test
    void testGetInstanceShouldReturnSameInstance() {
        SecUtils instance1 = SecUtils.getInstance();
        SecUtils instance2 = SecUtils.getInstance();
        assertSame(instance1, instance2);
    }

    @Test
    void testSignWithKeyPair() throws Exception {
        KeyPair keyPair = generateKeyPair();
        byte[] data = "sample data".getBytes();

        byte[] signature = secUtils.sign(data, keyPair);
        assertNotNull(signature);
        assertTrue(signature.length > 0);

        Signature verifier = Signature.getInstance("SHA256WithRSA");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(data);
        assertTrue(verifier.verify(signature));
    }

    @Test
    void testSignWithPrivateKey() throws Exception {
        KeyPair keyPair = generateKeyPair();
        byte[] data = "test data".getBytes();

        byte[] signature = secUtils.sign(data, keyPair.getPrivate());
        assertNotNull(signature);
        assertTrue(signature.length > 0);

        Signature verifier = Signature.getInstance("SHA256WithRSA");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(data);
        assertTrue(verifier.verify(signature));
    }


    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }
}
