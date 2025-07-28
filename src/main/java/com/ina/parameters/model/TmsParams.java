package com.ina.parameters.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TmsParams {
    private List<AidData> aids;
    private List<RidData> cpks;
    private MerchantTerminalData terminalConfig;
    private MerchantDetails merchantDetails;
}
