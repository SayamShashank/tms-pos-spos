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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.ina.common.constants.AppErrorConstants.*;
import static com.ina.common.utils.CommonUtils.getApiOutContext;

@Service
@Slf4j
public class DeviceSetUpService extends CommonValidator<DeviceTMSInitRequest> {

    private final InitService initService;

    protected DeviceSetUpService(InaPayMessages messages, InitService initService) {
        super(messages);
        this.initService = initService;
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

            apiOutContext = getApiOutContext(inputRefId, DEVICE_TMS_INIT_SUCCESS,
                    messages, messages.get(SUCCESS_CODE));
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId, DEVICE_TMS_INIT_FAILED,
                    messages, messages.get(FAILED_CODE));
        }

        response.setApiOutContext(apiOutContext);
        response.setSignedCertMetadata(signedCertMetadata);
        log.info("Triggering event");
        return response;

    }

    @Override
    public void evaluate(DeviceTMSInitRequest request) throws CommonValidationException {
        //Intentionally left blank

    }

}
