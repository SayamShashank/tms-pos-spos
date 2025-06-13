package com.ina.certificates.service;


import com.ina.common.crypto.model.certs.*;
import com.ina.common.crypto.service.CertGenerationService;
import com.ina.common.crypto.service.ServerEncryptionAndSignatureCertificateService;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiInContext;
import com.ina.common.model.ApiOutContext;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.Request;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.validator.CommonValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.ina.common.constants.AppErrorConstants.*;
import static com.ina.common.utils.CommonUtils.getApiOutContext;
import static java.util.Objects.nonNull;

@Service
@Slf4j
public class InitialiseCertificateService extends CommonValidator<ApiInContext> {


    private final CertGenerationService certGenerationService;

    private final ServerEncryptionAndSignatureCertificateService serverEncryptionAndSignatureCertificateService;

    protected InitialiseCertificateService(InaPayMessages messages,
                                           CertGenerationService certGenerationService,
                                           ServerEncryptionAndSignatureCertificateService serverEncryptionAndSignatureCertificateService) {
        super(messages);
        this.certGenerationService = certGenerationService;
        this.serverEncryptionAndSignatureCertificateService = serverEncryptionAndSignatureCertificateService;
    }

    @Override
    public void evaluate(ApiInContext apiInContext) throws CommonValidationException {
        // This method is intentionally left empty because it will be implemented in a subclass.
    }

    public CommonResponse generateServerCerts(InitialiseCertRequest request){
        CommonResponse response = new CommonResponse();
        CertificateGenerationResponse certificateGenerationResponse;
        ApiOutContext apiOutContext;
        String inputRefId = request.getApiInContext().getInputRefId();
        try{
            certificateGenerationResponse = certGenerationService
                    .generateCertificate(request.getCertificateType(), request.getApiInContext());
            if (certificateGenerationResponse.getStatus().equalsIgnoreCase("success")) {
                apiOutContext = getApiOutContext(inputRefId, CERTIFICATES_GENERATED_SUCCESSFULLY, messages, messages.get(SUCCESS_CODE));

            } else{
                apiOutContext = getApiOutContext(inputRefId,CERTIFICATES_GENERATION_FAILED, messages, messages.get(FAILED_CODE));

            }
        } catch(CommonValidationException commonValidationException){
            apiOutContext = getApiOutContext(inputRefId,commonValidationException.getCode(),
                    messages.get(commonValidationException.getCode()), messages.get(FAILED_CODE));
        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

    public CommonResponse generateRootCertificate(PublishRootCertificateRequest request){

        CommonResponse response = new CommonResponse();
        CertificateGenerationResponse certificateGenerationResponse;
        CertChain certChain = rootCAChainCert(request);
        ApiOutContext apiOutContext;
        String inputRefId = request.getApiInContext().getInputRefId();
        try{
            certificateGenerationResponse =
                    certGenerationService.saveCertificatesIntoDB(certChain, "ECP521", request.getApiInContext());
            if (certificateGenerationResponse.getStatus().equalsIgnoreCase("success")) {
                apiOutContext = getApiOutContext(inputRefId,CERTIFICATES_GENERATED_SUCCESSFULLY, messages, messages.get(SUCCESS_CODE));
            } else{
                apiOutContext = getApiOutContext(inputRefId,CERTIFICATES_GENERATION_FAILED, messages, messages.get(FAILED_CODE));
            }
        } catch(CommonValidationException commonValidationException){
            apiOutContext = getApiOutContext(inputRefId,commonValidationException.getCode(),
                    messages.get(commonValidationException.getCode()), messages.get(FAILED_CODE));
        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

    private static CertChain rootCAChainCert(PublishRootCertificateRequest request) {
        return CertChain.builder()
                .cert(request.getRootCertificateResponse().getCert())
                .skLmk(request.getRootCertificateResponse().getSkLmk())
                .certLevel("ROOT_CA")
                .build();
    }

    public CommonResponse generateL4Certs(InitialiseCertRequest initialiseCertRequest){
        CommonResponse response = new CommonResponse();
        ApiOutContext apiOutContext;
        String inputRefId = initialiseCertRequest.getApiInContext().getInputRefId();
        try {
            ServerCertsInfoResponse serverCertsInfoResponse = serverEncryptionAndSignatureCertificateService
                    .generateServerL4Certificates(initialiseCertRequest.getCertificateType(), initialiseCertRequest.getApiInContext());

            if (serverCertsInfoResponse.getApiOutContext().getStatus().equalsIgnoreCase(messages.get(SUCCESS_CODE))) {
                apiOutContext = serverCertsInfoResponse.getApiOutContext();
            } else{
                apiOutContext = getApiOutContext(inputRefId,CERTIFICATES_GENERATION_FAILED, messages, messages.get(FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId,exception.getCode(),exception.getMessage(), messages.get(FAILED_CODE));

        }

        response.setApiOutContext(apiOutContext);
        return response;
    }


    public ServerCertsResponse getAllServerCerts(Request request) {
        ServerCertsResponse allServerCerts = certGenerationService.getAllServerCerts();
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        apiOutContext.setCode(SUCCESS_CODE);
        apiOutContext.setMessage(messages.get(SUCCESS_CODE));
        allServerCerts.setApiOutContext(apiOutContext);
        return allServerCerts;
    }

    public ServerCertsStatusResponse getServerCertificateStatus(InitialiseCertRequest certRequest) {
        ServerCertsStatusResponse serverCertificateStatus =
                certGenerationService.getServerCertificateStatus(certRequest.getCertificateType());
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(certRequest.getApiInContext().getInputRefId());
        apiOutContext.setCode(SUCCESS_CODE);
        apiOutContext.setMessage(messages.get(SUCCESS_CODE));
        serverCertificateStatus.setApiOutContext(apiOutContext);
        return serverCertificateStatus;
    }

    public ViewCertificateInfo viewCertInfo(InitialiseCertRequest certRequest) {

        ApiOutContext apiOutContext;
        ViewCertificateInfo certificateInfo = new ViewCertificateInfo();
        String inputRefId = certRequest.getApiInContext().getInputRefId();
        try {
            certificateInfo = certGenerationService.
                    viewCertificate(certRequest.getCertificateType(), inputRefId);
            if(nonNull(certificateInfo)){
                apiOutContext = getApiOutContext(inputRefId,CERTIFICATE_RETRIEVED_SUCCESSFULLY,
                        messages.get(CERTIFICATE_RETRIEVED_SUCCESSFULLY), messages.get(SUCCESS_CODE));
            } else{
                apiOutContext = getApiOutContext(inputRefId,CERTIFICATE_IS_NOT_AVAILABLE,
                        messages.get(CERTIFICATE_IS_NOT_AVAILABLE), messages.get(FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId,exception.getCode(), messages.get(exception.getCode()), messages.get(FAILED_CODE));
        }
        certificateInfo.setApiOutContext(apiOutContext);
        return certificateInfo;
    }
}
