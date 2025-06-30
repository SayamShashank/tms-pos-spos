package com.ina.parameters.service;

import com.ina.common.response.message.InaPayMessages;
import com.ina.common.utils.CommonUtils;
import com.ina.dao.EMVParametersRepository;
import com.ina.dao.entity.EMVParameters;
import com.ina.parameters.model.GetParamChecksumRequest;
import com.ina.parameters.model.ParamChecksumResponse;
import org.springframework.stereotype.Service;

import static com.ina.constants.AppErrorConstants.CHECKSUM_NOT_FOUND;
import static com.ina.constants.AppErrorConstants.SUCCESS_CODE;
import static com.ina.util.TMSUtil.throwValidationException;
import static java.util.Objects.nonNull;

@Service
public class GetParamChecksumService {
    private final EMVParametersRepository emvParametersRepository;
    private final InaPayMessages inaPayMessages;

    public GetParamChecksumService(EMVParametersRepository emvParametersRepository, InaPayMessages inaPayMessages) {
        this.emvParametersRepository = emvParametersRepository;
        this.inaPayMessages = inaPayMessages;
    }
    public ParamChecksumResponse getParamChecksum(GetParamChecksumRequest request){
        EMVParameters emvParameters = emvParametersRepository.findByTrsMidAndTerminalIdAndDeviceId(
                request.getTrsMid(),
                request.getTid(),
                request.getDeviceMetadata().getDeviceId());
        if (nonNull(emvParameters)) {
            ParamChecksumResponse response = new ParamChecksumResponse();
            response.setParamChecksum(emvParameters.getParamCheckSum());
            response.setApiOutContext(CommonUtils.getApiOutContext(
                    request.getApiInContext().getInputRefId(),
                    SUCCESS_CODE,
                    inaPayMessages.get(SUCCESS_CODE)));
            return response;
        }else {
             throw throwValidationException(
                    request.getApiInContext().getInputRefId(),
                    CHECKSUM_NOT_FOUND,
                    inaPayMessages);
        }

    }
}
