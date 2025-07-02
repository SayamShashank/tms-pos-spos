package com.ina.keys.service;

import com.ina.common.constants.AppConstants;
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

import static com.ina.common.constants.AppErrorConstants.*;
import static com.ina.common.utils.CommonUtils.getApiOutContext;
import static com.ina.constants.AppConstants.TMS;

@Service
@Slf4j
public class SPOSAuthKeyService extends CommonValidator<SPOSAuthKeyRequest> {

   private final SPOSAuthenticationKeys sposAuthenticationKeys;

   private final CryptoUtils cryptoUtils;

   private final InaPayMessages inaPayMessages;

    protected SPOSAuthKeyService(InaPayMessages inaPayMessages, CryptoUtils cryptoUtils,
                                 SPOSAuthenticationKeys sposAuthenticationKeys) {
        super(inaPayMessages);
        this.cryptoUtils = cryptoUtils;
        this.sposAuthenticationKeys = sposAuthenticationKeys;
        this.inaPayMessages = inaPayMessages;
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

        ApiOutContext apiOutContext;

        String inputRefId = sposAuthKeyRequest.getApiInContext().getInputRefId();
        try {
            ServerCertsGenerationResponse serverCertsGenerationResponse =
                    cryptoUtils.updateServerCertificates(certChain, "ECP256", sposAuthKeyRequest.getApiInContext());
            if(serverCertsGenerationResponse.getStatusCode().equalsIgnoreCase("200")){
                apiOutContext = getApiOutContext(inputRefId,AUTHENTICATION_GENERATION_SUCCESSFUL, inaPayMessages, inaPayMessages.get(SUCCESS_CODE), TMS);

            } else{
                apiOutContext = getApiOutContext(inputRefId,AUTHENTICATION_GENERATION_FAILED, inaPayMessages, inaPayMessages.get(FAILED_CODE), TMS);

            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId,exception.getCode(), exception.getMessage(), inaPayMessages.get(FAILED_CODE), TMS);

        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

    public CommonResponse getSPOSAuthKey(FetchSPOSAuthKeyRequest request){
        CommonResponse response = new CommonResponse();
        ApiOutContext apiOutContext;

        SPOSAuthKeyInfo responseKey;
        String inputRefId = request.getApiInContext().getInputRefId();
        try {
            responseKey = sposAuthenticationKeys.
                    getSPOSAuthenticationKeys(request.getAuthKeyType(), inputRefId);

            if(responseKey.getStatusCode().equalsIgnoreCase("200")){
                apiOutContext = getApiOutContext(inputRefId,AUTHENTICATION_KEYS_RETRIEVED, inaPayMessages, inaPayMessages.get(SUCCESS_CODE), TMS);

            } else{
                apiOutContext = getApiOutContext(inputRefId,AUTHENTICATION_KEYS_RETRIEVED_FAILED, inaPayMessages, inaPayMessages.get(FAILED_CODE), TMS);

            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId,exception.getCode(), exception.getMessage(), inaPayMessages.get(FAILED_CODE), TMS);

        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

}
