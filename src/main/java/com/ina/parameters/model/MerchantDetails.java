package com.ina.parameters.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDetails {
    private String merchantNameEN;
    private String merchantNameAR;
    private String merchantNameAddress1EN;
    private String merchantNameAddress2EN;
    private String merchantNameAddress1AR;
    private String merchantNameAddress2AR;
    private String merchantNameCityEN;
    private String merchantNameCityAR;
    private String merchantPostalCode;
}
