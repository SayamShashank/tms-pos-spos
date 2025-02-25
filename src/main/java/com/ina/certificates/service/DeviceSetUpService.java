package com.ina.certificates.service;

import com.ina.certificates.model.DeviceTMSInitRequest;
import com.ina.certificates.model.DeviceTMSInitResponse;
import com.ina.common.constants.AppErrorConstants;
import com.ina.common.crypto.model.init.SignedCertMetadata;
import com.ina.common.crypto.service.InitService;
import com.ina.common.enums.CertTypeAndLevel;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiOutContext;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.validator.CommonValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeviceSetUpService extends CommonValidator<DeviceTMSInitRequest> {

    private final InitService initService;

    protected DeviceSetUpService(InaPayMessages messages, InitService initService) {
        super(messages);
        this.initService = initService;
    }

    public DeviceTMSInitResponse deviceTXNInit(DeviceTMSInitRequest request) {
        log.info("Inside getTransportKeys method");

        evaluate(request);

        DeviceTMSInitResponse response = new DeviceTMSInitResponse();
        ApiOutContext apiOutContext = new ApiOutContext();

        SignedCertMetadata signedCertMetadata = null;
        try {
            signedCertMetadata = initService.initProcess(request.getCertCSRMetadata(),
                    CertTypeAndLevel.TMS_INIT.getCertType(), request.getApiInContext().getInputRefId());

            apiOutContext.setCode(AppErrorConstants.SUCCESS_CODE);
            apiOutContext.setMessage(messages.get(AppErrorConstants.SUCCESS_CODE));
            apiOutContext.setStatus(messages.get(AppErrorConstants.SUCCESS_CODE));
        } catch (CommonValidationException exception) {
            apiOutContext.setCode(exception.getCode());
            apiOutContext.setMessage(exception.getMessage());
            apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
        }
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());

        response.setApiOutContext(apiOutContext);
        response.setSignedCertMetadata(signedCertMetadata);
        log.info("Triggering event");
        return response;

    }

    @Override
    public void evaluate(DeviceTMSInitRequest request) throws CommonValidationException {
        // TODO Auto-generated method stub

    }

}
