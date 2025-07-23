package com.ina.certificates.service;

import com.ina.certificates.model.DeviceTMSInitRequest;
import com.ina.certificates.model.DeviceTMSInitResponse;
import com.ina.common.crypto.model.init.SignedCertMetadata;
import com.ina.common.crypto.service.InitService;
import com.ina.common.enums.CertTypeAndLevel;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiOutContext;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.validator.CommonValidator;
import com.ina.common.validator.DeviceProfileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.ina.common.constants.AppErrorConstants.*;
import static com.ina.common.utils.CommonUtils.getApiOutContext;
import static com.ina.constants.AppConstants.TMS;

@Service
@Slf4j
public class DeviceSetUpService extends CommonValidator<DeviceTMSInitRequest> {

    private final InitService initService;

    private final DeviceProfileValidator deviceProfileValidator;

    protected DeviceSetUpService(InaPayMessages messages, InitService initService, DeviceProfileValidator deviceProfileValidator) {
        super(messages);
        this.initService = initService;
        this.deviceProfileValidator = deviceProfileValidator;
    }

    public DeviceTMSInitResponse deviceTMSInit(DeviceTMSInitRequest request) {
        log.info("TMS init process started...!");

        evaluate(request);

        DeviceTMSInitResponse response = new DeviceTMSInitResponse();
        ApiOutContext apiOutContext;

        SignedCertMetadata signedCertMetadata = null;
        String inputRefId = request.getApiInContext().getInputRefId();
        try {
            signedCertMetadata = initService.initProcess(request.getCertCSRMetadata(),
                    CertTypeAndLevel.TMS_INIT.getCertType(), inputRefId, request.getDeviceMetadata().getDeviceId());

            apiOutContext = getApiOutContext(inputRefId, DEVICE_INIT_IS_SUCCESSFUL,
                    messages, messages.get(SUCCESS_CODE), TMS);
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId, DEVICE_INIT_IS_SUCCESSFUL,
                    messages, messages.get(FAILED_CODE), TMS);
        }

        response.setApiOutContext(apiOutContext);
        response.setSignedCertMetadata(signedCertMetadata);
        log.info("Triggering event");
        return response;

    }

    @Override
    public void evaluate(DeviceTMSInitRequest request) throws CommonValidationException {
        String inputRefId = request.getApiInContext().getInputRefId();

        deviceProfileValidator.timeStampFreshnessCheck(inputRefId, request.getApiInContext().getTimeStamp());

        deviceProfileValidator.checkDeviceProfileFlagsForTMSINIT(request.getDeviceMetadata().getDeviceId(), inputRefId);

    }

}
