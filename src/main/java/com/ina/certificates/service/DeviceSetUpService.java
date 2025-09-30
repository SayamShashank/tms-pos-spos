package com.ina.certificates.service;

import com.ina.certificates.model.DeviceTMSInitRequest;
import com.ina.certificates.model.DeviceTMSInitResponse;
import com.ina.common.crypto.entity.AppReleaseDetails;
import com.ina.common.crypto.model.init.SignedCertMetadata;
import com.ina.common.crypto.repository.AppReleaseDetailsRepository;
import com.ina.common.crypto.service.InitService;
import com.ina.common.enums.CertTypeAndLevel;
import com.ina.common.enums.NextCommandDetails;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiOutContext;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.validator.CommonValidator;
import com.ina.common.validator.DeviceProfileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.ina.common.constants.AppErrorConstants.*;
import static com.ina.common.utils.CommonUtils.getApiOutContext;
import static com.ina.common.utils.CommonUtils.throwValidationException;
import static com.ina.constants.AppConstants.TMS;
import static java.util.Objects.isNull;

@Service
@Slf4j
public class DeviceSetUpService extends CommonValidator<DeviceTMSInitRequest> {

    private final InitService initService;

    private final DeviceProfileValidator deviceProfileValidator;
    private final AppReleaseDetailsRepository appReleaseDetailsRepository;

    protected DeviceSetUpService(InaPayMessages messages, InitService initService, DeviceProfileValidator deviceProfileValidator, AppReleaseDetailsRepository appReleaseDetailsRepository) {
        super(messages);
        this.initService = initService;
        this.deviceProfileValidator = deviceProfileValidator;
        this.appReleaseDetailsRepository = appReleaseDetailsRepository;
    }

    public DeviceTMSInitResponse deviceTMSInit(DeviceTMSInitRequest request) {
        log.info("TMS init process started...!");

        evaluate(request);

        DeviceTMSInitResponse response = new DeviceTMSInitResponse();
        ApiOutContext apiOutContext;

        SignedCertMetadata signedCertMetadata = null;
        String inputRefId = request.getApiInContext().getInputRefId();
        AppReleaseDetails releaseDetails = appReleaseDetailsRepository.findByIsExpiredFalse();
        try {
            signedCertMetadata = initService.initProcess(request.getCertCSRMetadata(),
                    CertTypeAndLevel.TMS_INIT.getCertType(),
                    inputRefId,
                    request.getDeviceMetadata().getDeviceId(),
                    releaseDetails.getEndDate());

            apiOutContext = getApiOutContext(inputRefId, DEVICE_INIT_IS_SUCCESSFUL,
                    messages, SUCCESS_CODE, NextCommandDetails.NONE);
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId, DEVICE_INIT_IS_SUCCESSFUL,
                    messages, FAILED_CODE, NextCommandDetails.ERASE);
        }

        response.setApiOutContext(apiOutContext);
        response.setSignedCertMetadata(signedCertMetadata);
        log.info("Triggering event");
        return response;

    }

    @Override
    public void evaluate(DeviceTMSInitRequest request) throws CommonValidationException {
        log.info("Evaluating passed request for time freshness check...!");
        String inputRefId = request.getApiInContext().getInputRefId();
        if (isNull(request.getApiInContext().getTimeStamp())) {
            throw throwValidationException(inputRefId, TIME_STAMP_IS_NOT_AVAILABLE_IN_REQUEST,
                    messages, NextCommandDetails.BLOCK, FAILED_CODE);
        }
        deviceProfileValidator.timeStampFreshnessCheck(inputRefId, request.getApiInContext().getTimeStamp());
        deviceProfileValidator.checkDeviceProfileFlagsForTMSINIT(request.getDeviceMetadata().getDeviceId(), inputRefId);
    }

}
