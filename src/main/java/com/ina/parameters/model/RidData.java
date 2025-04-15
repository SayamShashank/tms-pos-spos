package com.ina.parameters.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RidData {
    private String rid;//rid
    private String keyId;//keyIndex
    private String rsaArithmeticIndex;//hashId
    private String module;//publicKey
    private String expiry;//expiry
    private String checksum;//checksum
    private String exponent;//exponent
}
