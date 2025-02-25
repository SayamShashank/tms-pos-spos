package com.ina.keys.service;



import com.ina.common.constants.AppErrorConstants;
import com.ina.common.crypto.model.aekdek.AvailableServerKeysResponse;
import com.ina.common.crypto.model.aekdek.GenerateAEKAndDEKInfo;
import com.ina.common.crypto.model.aekdek.GenerateAEKAndDEKResponse;
import com.ina.common.crypto.service.AEKAndDEKService;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiOutContext;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.Request;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.validator.CommonValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import static com.ina.common.constants.AppErrorConstants.*;
import static com.ina.util.TMSUtil.getApiOutContext;

@Service
@Slf4j
public class AEKAndDEKKeysService extends CommonValidator<Request> {

    private final AEKAndDEKService aekAndDEKService;

    protected AEKAndDEKKeysService(InaPayMessages messages,AEKAndDEKService aekAndDEKService) {
        super(messages);
        this.aekAndDEKService = aekAndDEKService;
    }

    @Override
    public void evaluate(Request request) throws CommonValidationException {
        // This method is intentionally left empty because it will be implemented in a subclass.
    }

    public CommonResponse generateDEKAndAEKKey(Request request) {
        CommonResponse response = new CommonResponse();
        ApiOutContext apiOutContext = new ApiOutContext();
        try {
            GenerateAEKAndDEKResponse generateAEKAndDEKResponse =
                    aekAndDEKService.generateAEKAndDEK("TXN", request.getApiInContext().getInputRefId());
            Set<String> aekAndDEKInfos = generateAEKAndDEKResponse.getGenerateAEKAndDEKInfos()
                    .stream()
                    .map(GenerateAEKAndDEKInfo::getStatusCode)
                    .collect(Collectors.toSet());

            if (!aekAndDEKInfos.contains("500")) {
                apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),AEK_AND_DEK_KEY_GENERATION_SUCCESSFUL, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.SUCCESS_CODE));

            } else{
                apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),AEK_KEY_GENERATION_FAILED, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),exception.getCode(), exception.getMessage());
            apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

    public CommonResponse rotateDEK(Request request) {
        CommonResponse response = new CommonResponse();
        ApiOutContext apiOutContext = new ApiOutContext();
        GenerateAEKAndDEKResponse aekDekKey = new GenerateAEKAndDEKResponse();
        try {
            aekDekKey = aekAndDEKService.rotateDEK("TXN", request.getApiInContext().getInputRefId());
            Set<String> aekAndDEKInfos = aekDekKey.getGenerateAEKAndDEKInfos()
                    .stream()
                    .map(GenerateAEKAndDEKInfo::getStatusCode)
                    .collect(Collectors.toSet());
            if (!aekAndDEKInfos.contains("500")) {
                apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),AEK_AND_DEK_KEY_ROTATION_I_SUCCESSFUL, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.SUCCESS_CODE));
            } else{
                apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),AEK_AND_DEK_KEY_ROTATION_FAILED, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),exception.getCode(), exception.getMessage());
            apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

    public AvailableServerKeysResponse getAllAEKAndDEkServerKeys(Request request) {
        AvailableServerKeysResponse availableKeys = aekAndDEKService.getAvailableKeys();
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        apiOutContext.setCode(AppErrorConstants.SUCCESS_CODE);
        apiOutContext.setMessage(messages.get(AppErrorConstants.SUCCESS_CODE));
        availableKeys.setApiOutContext(apiOutContext);
        return availableKeys;
    }
}

