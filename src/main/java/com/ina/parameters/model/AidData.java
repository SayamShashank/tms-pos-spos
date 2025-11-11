package com.ina.parameters.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AidData {
    private String aid;//
    private String applicationName;//aidLabel
    private String cardSchemeAcquirerId;//cardSchemeAcquirerId
    private String securityCapability;
    private String addTerminalCapability;//addTerminalCapability
    private String terminalCapability;//terminalCapability
    private String tacDenial;//denialActionCode
    private String tacOnline;//onlineActionCode
    private String tacDefault;//defaultActionCode
    private String cvmLimit;//termCVMRequiredLimit
    private String floorLimit;//terminalContactlessFloorLimit
    private String transactionLimit;//termContactlessTxnLimit
    private String emvTerminalType;//emvTerminalType
    private String ttq;
    private String limitOnDevice;

    private String limitNoOnDevice;
    private String posEntryMode;
    private String kernelId;
    private String cvmSupported;
    private String terminalRiskMgmnt;
}



