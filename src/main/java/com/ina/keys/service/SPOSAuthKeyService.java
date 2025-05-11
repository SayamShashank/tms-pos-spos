package com.ina.keys.service;

import com.ina.common.constants.AppErrorConstants;
import com.ina.common.crypto.model.certs.CertChain;
import com.ina.common.crypto.model.certs.ServerCertsGenerationResponse;
import com.ina.common.crypto.model.keys.FetchSPOSAuthKeyRequest;
import com.ina.common.crypto.model.keys.SPOSAuthKeyInfo;
import com.ina.common.crypto.model.keys.SPOSAuthKeyRequest;
import com.ina.common.crypto.service.SPOSAuthenticationKeys;
import com.ina.common.crypto.util.CryptoUtils;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiOutContext;
import com.ina.common.model.CommonResponse;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.validator.CommonValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.ina.common.constants.AppErrorConstants.*;
import static com.ina.util.TMSUtil.getApiOutContext;

@Service
@Slf4j
public class SPOSAuthKeyService extends CommonValidator<SPOSAuthKeyRequest> {

   private final SPOSAuthenticationKeys sposAuthenticationKeys;

   private final CryptoUtils cryptoUtils;

   private final InaPayMessages messages;

    protected SPOSAuthKeyService(InaPayMessages messages, CryptoUtils cryptoUtils,
                                 SPOSAuthenticationKeys sposAuthenticationKeys) {
        super(messages);
        this.cryptoUtils = cryptoUtils;
        this.sposAuthenticationKeys = sposAuthenticationKeys;
        this.messages = messages;
    }


    @Override
    public void evaluate(SPOSAuthKeyRequest authKeyRequest) throws CommonValidationException {
        // This method is intentionally left empty because it will be implemented in a subclass.
    }

    public CommonResponse generateSPOSAuthKey(SPOSAuthKeyRequest sposAuthKeyRequest){
        CommonResponse response = new CommonResponse();

        CertChain certChain = CertChain.builder().certLevel(sposAuthKeyRequest.getAuthKeyType())
                .cert(sposAuthKeyRequest.getPubKey())
                .skLmk(sposAuthKeyRequest.getExportedPvtKey()).build();
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(sposAuthKeyRequest.getApiInContext().getInputRefId());

        try {
            ServerCertsGenerationResponse serverCertsGenerationResponse =
                    cryptoUtils.updateServerCertificates(certChain, "ECP256", sposAuthKeyRequest.getApiInContext());
            if(serverCertsGenerationResponse.getStatusCode().equalsIgnoreCase("200")){
                apiOutContext = getApiOutContext(sposAuthKeyRequest.getApiInContext().getInputRefId(),AUTHENTICATION_GENERATION_SUCCESSFUL, messages);
                apiOutContext.setStatus(messages.get(SUCCESS_CODE));
            } else{
                apiOutContext = getApiOutContext(sposAuthKeyRequest.getApiInContext().getInputRefId(),AUTHENTICATION_GENERATION_FAILED, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(sposAuthKeyRequest.getApiInContext().getInputRefId(),exception.getCode(), exception.getMessage());
            apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
        }
        apiOutContext.setTimeStamp(String.valueOf(LocalDateTime.now(ZoneId.systemDefault())));
        response.setApiOutContext(apiOutContext);
        return response;
    }

    public CommonResponse getSPOSAuthKey(FetchSPOSAuthKeyRequest request){
        CommonResponse response = new CommonResponse();
        ApiOutContext apiOutContext = new ApiOutContext();

        SPOSAuthKeyInfo responseKey = new SPOSAuthKeyInfo();
        try {
            responseKey = sposAuthenticationKeys.
                    getSPOSAuthenticationKeys(request.getAuthKeyType(), request.getApiInContext().getInputRefId());
            if(responseKey.getStatusCode().equalsIgnoreCase("200")){
                apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),AUTHENTICATION_KEYS_RETRIEVED, messages);
                apiOutContext.setStatus(messages.get(SUCCESS_CODE));
            } else{
                apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),AUTHENTICATION_KEYS_RETRIEVED_FAILED, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),exception.getCode(), exception.getMessage());
            apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

}
