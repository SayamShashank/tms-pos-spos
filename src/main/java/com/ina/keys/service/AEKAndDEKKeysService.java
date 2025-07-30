package com.ina.keys.service;



import com.ina.certificates.model.DeviceTMSInitRequest;
import com.ina.common.crypto.model.aekdek.AvailableServerKeysResponse;
import com.ina.common.crypto.model.aekdek.GenerateAEKAndDEKInfo;
import com.ina.common.crypto.model.aekdek.GenerateAEKAndDEKResponse;
import com.ina.common.crypto.service.AEKAndDEKService;
import com.ina.common.enums.NextCommandDetails;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiOutContext;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.Request;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.validator.CommonValidator;
import com.ina.common.validator.DeviceProfileValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static com.ina.common.constants.AppConstants.TMS;
import static com.ina.common.constants.AppErrorConstants.*;
import static com.ina.common.utils.CommonUtils.getApiOutContext;
import static com.ina.common.utils.CommonUtils.throwValidationException;
import static java.util.Objects.isNull;

@Service
@Slf4j
public class AEKAndDEKKeysService extends CommonValidator<Request> {

    private final AEKAndDEKService aekAndDEKService;

    private final DeviceProfileValidator deviceProfileValidator;

    protected AEKAndDEKKeysService(InaPayMessages messages,AEKAndDEKService aekAndDEKService,
                                   DeviceProfileValidator deviceProfileValidator) {
        super(messages);
        this.aekAndDEKService = aekAndDEKService;
        this.deviceProfileValidator=deviceProfileValidator;
    }

    @Override
    public void evaluate(Request request) throws CommonValidationException {

            String inputRefId = request.getApiInContext().getInputRefId();
            if (isNull(request.getApiInContext().getTimeStamp())){
                throw throwValidationException(inputRefId, TIME_STAMP_IS_NOT_AVAILABLE_IN_REQUEST,
                        messages, NextCommandDetails.BLOCK);
            }

            deviceProfileValidator.timeStampFreshnessCheck(inputRefId, request.getApiInContext().getTimeStamp());


    }

    public CommonResponse generateDEKAndAEKKey(Request request) {
        CommonResponse response = new CommonResponse();
        ApiOutContext apiOutContext;
        String inputRefId = request.getApiInContext().getInputRefId();
        try {
            GenerateAEKAndDEKResponse generateAEKAndDEKResponse =
                    aekAndDEKService.generateAEKAndDEK(TMS, inputRefId);
            Set<String> aekAndDEKInfos = generateAEKAndDEKResponse.getGenerateAEKAndDEKInfos()
                    .stream()
                    .map(GenerateAEKAndDEKInfo::getStatusCode)
                    .collect(Collectors.toSet());

            if (!aekAndDEKInfos.contains("500")) {
                apiOutContext = getApiOutContext(inputRefId,AEK_AND_DEK_KEY_GENERATION_SUCCESSFUL,
                        messages, messages.get(SUCCESS_CODE));

            } else{
                apiOutContext = getApiOutContext(inputRefId,AEK_KEY_GENERATION_FAILED, messages, messages.get(FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId,exception.getCode(), exception.getMessage(), messages.get(FAILED_CODE));
        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

    public CommonResponse rotateDEK(Request request) {
        CommonResponse response = new CommonResponse();
        ApiOutContext apiOutContext;
        GenerateAEKAndDEKResponse aekDekKey;
        String inputRefId = request.getApiInContext().getInputRefId();
        try {
            aekDekKey = aekAndDEKService.rotateDEK(TMS, inputRefId);

            Set<String> aekAndDEKInfos = aekDekKey.getGenerateAEKAndDEKInfos()
                    .stream()
                    .map(GenerateAEKAndDEKInfo::getStatusCode)
                    .collect(Collectors.toSet());

            if (!aekAndDEKInfos.contains("500")) {
                apiOutContext = getApiOutContext(inputRefId,AEK_AND_DEK_KEY_ROTATION_IS_SUCCESSFUL, messages, messages.get(SUCCESS_CODE));
            } else{
                apiOutContext = getApiOutContext(inputRefId,FAILED_TO_ROTATE_AEK_AND_DEK_KEY, messages, messages.get(SUCCESS_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId,exception.getCode(), exception.getMessage(), messages.get(SUCCESS_CODE));
        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

    public AvailableServerKeysResponse getAllAEKAndDEkServerKeys(Request request) {
        AvailableServerKeysResponse availableKeys = aekAndDEKService.getAvailableKeys();
        ApiOutContext apiOutContext =  getApiOutContext(request.getApiInContext().getInputRefId(), SUCCESS_CODE,messages,
        messages.get(SUCCESS_CODE));
        availableKeys.setApiOutContext(apiOutContext);
        return availableKeys;
    }
}

