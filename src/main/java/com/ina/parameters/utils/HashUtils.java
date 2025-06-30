package com.ina.parameters.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ina.constants.AppConstants.SHA_512_ALGORITHM;

@Component
@Slf4j
public class HashUtils {


    public  String generateHashWithSHA512(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(SHA_512_ALGORITHM);
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            String hashText = IntStream.range(0, messageDigest.length)
                    .mapToObj(i -> {
                        String hex = Integer.toHexString(0xff & messageDigest[i]);
                        return hex.length() == 1 ? "0" + hex : hex;
                    })
                    .collect(Collectors.joining());

            log.info("parameter checksum===>" + hashText);
            return hashText;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not found", e);
        }
    }
}
