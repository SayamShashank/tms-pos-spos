package com.ina.parameters.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MerchantTerminalData {
    private String merchantId;
    private String terminalId;
    private String terminalCurrencyCode;
    private String merchantCategoryCode;
    private String merchantNameAndLocation;
    private String terminalCountryCode;
}
