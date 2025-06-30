package com.ina.parameters.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class HashUtilsTest {
    @InjectMocks
    HashUtils hashUtils;

    @Test
    void testGenerateHashWithSHA512_returnsNotNullOrEmpty() {
        String input = "testString";
        String hash = hashUtils.generateHashWithSHA512(input);

        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    void testGenerateHashWithSHA512_sameInputSameHash() {
        String input = "repeatableInput";
        String hash1 = hashUtils.generateHashWithSHA512(input);
        String hash2 = hashUtils.generateHashWithSHA512(input);

        assertEquals(hash1, hash2, "Hash should be consistent for same input");
    }

    @Test
    void testGenerateHashWithSHA512_differentInputDifferentHash() {
        String input1 = "firstInput";
        String input2 = "secondInput";
        String hash1 = hashUtils.generateHashWithSHA512(input1);
        String hash2 = hashUtils.generateHashWithSHA512(input2);

        assertNotEquals(hash1, hash2, "Different inputs should produce different hashes");
    }

    @Test
    void testGenerateHashWithSHA512_outputLength() {
        String input = "lengthTest";
        String hash = hashUtils.generateHashWithSHA512(input);

        assertEquals(128, hash.length(), "SHA-512 hash should be 128 hex characters long");
    }
}
