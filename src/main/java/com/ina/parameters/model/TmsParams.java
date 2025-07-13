package com.ina.parameters.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TmsParams {
    private List<AidList> aids;
    private List<RidList> cpks;
    private MerchantTerminalData terminalConfig;
    private MerchantDetails merchantDetails;
}
