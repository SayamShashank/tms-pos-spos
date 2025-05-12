package com.ina.certificates.service;


import com.ina.common.constants.AppErrorConstants;
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

import java.util.Set;
import java.util.stream.Collectors;

import static com.ina.common.constants.AppErrorConstants.*;
import static com.ina.util.TMSUtil.getApiOutContext;
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
        CertificateGenerationResponse certificateGenerationResponse = new CertificateGenerationResponse();
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setTimeStamp(request.getApiInContext().getTimeStamp());
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        try{
            certificateGenerationResponse = certGenerationService
                    .generateCertificate(request.getCertificateType(), request.getApiInContext());
            if (certificateGenerationResponse.getStatus().equalsIgnoreCase("success")) {
                apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),CERTIFICATES_GENERATED_SUCCESSFULLY, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.SUCCESS_CODE));
            } else{
                apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),CERTIFICATES_GENERATION_FAILED, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
            }
        } catch(CommonValidationException commonValidationException){
            apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),commonValidationException.getCode(),
                    messages.get(commonValidationException.getCode()));
            apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

    public CommonResponse generateRootCertificate(PublishRootCertificateRequest request){

        CommonResponse response = new CommonResponse();
        CertificateGenerationResponse certificateGenerationResponse = new CertificateGenerationResponse();
        CertChain certChain = CertChain.builder()
                .cert(request.getRootCertificateResponse().getCert())
                .skLmk(request.getRootCertificateResponse().getSkLmk())
                .certLevel("ROOT_CA")
                .build();
        ApiOutContext apiOutContext = new ApiOutContext();
        try{
            certificateGenerationResponse =
                    certGenerationService.saveCertificatesIntoDB(certChain, "ECP512", request.getApiInContext());
            if (certificateGenerationResponse.getStatus().equalsIgnoreCase("success")) {
                apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),CERTIFICATES_GENERATED_SUCCESSFULLY, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.SUCCESS_CODE));
            } else{
                apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),CERTIFICATES_GENERATION_FAILED, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
            }
        } catch(CommonValidationException commonValidationException){
            apiOutContext = getApiOutContext(request.getApiInContext().getInputRefId(),commonValidationException.getCode(),
                    messages.get(commonValidationException.getCode()));
            apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
        }
        response.setApiOutContext(apiOutContext);
        return response;
    }

    public CommonResponse generateL4Certs(InitialiseCertRequest initialiseCertRequest){
        CommonResponse response = new CommonResponse();
        ApiOutContext apiOutContext = new ApiOutContext();
        try {
            ServerCertsInfoResponse serverCertsInfoResponse = serverEncryptionAndSignatureCertificateService
                    .generateServerL4Certificates(initialiseCertRequest.getCertificateType(), initialiseCertRequest.getApiInContext());

            if (serverCertsInfoResponse.getApiOutContext().getStatus().equalsIgnoreCase(messages.get(SUCCESS_CODE))) {
                apiOutContext = serverCertsInfoResponse.getApiOutContext();
            } else{
                apiOutContext = getApiOutContext(initialiseCertRequest.getApiInContext().getInputRefId(),CERTIFICATES_GENERATION_FAILED, messages);
                apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(initialiseCertRequest.getApiInContext().getInputRefId(),exception.getCode(),exception.getMessage());
            apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
        }

        response.setApiOutContext(apiOutContext);
        return response;
    }


    public ServerCertsResponse getAllServerCerts(Request request) {
        ServerCertsResponse allServerCerts = certGenerationService.getAllServerCerts();
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        apiOutContext.setCode(AppErrorConstants.SUCCESS_CODE);
        apiOutContext.setMessage(messages.get(AppErrorConstants.SUCCESS_CODE));
        allServerCerts.setApiOutContext(apiOutContext);
        return allServerCerts;
    }

    public ServerCertsStatusResponse getServerCertificateStatus(InitialiseCertRequest certRequest) {
        ServerCertsStatusResponse serverCertificateStatus =
                certGenerationService.getServerCertificateStatus(certRequest.getCertificateType());
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(certRequest.getApiInContext().getInputRefId());
        apiOutContext.setCode(AppErrorConstants.SUCCESS_CODE);
        apiOutContext.setMessage(messages.get(AppErrorConstants.SUCCESS_CODE));
        serverCertificateStatus.setApiOutContext(apiOutContext);
        return serverCertificateStatus;
    }

    public ViewCertificateInfo viewCertInfo(InitialiseCertRequest certRequest) {

        ApiOutContext apiOutContext = new ApiOutContext();
        ViewCertificateInfo certificateInfo = new ViewCertificateInfo();
        String inputRefId = certRequest.getApiInContext().getInputRefId();
        try {
            certificateInfo = certGenerationService.
                    viewCertificate(certRequest.getCertificateType(), inputRefId);
            if(nonNull(certificateInfo)){
                apiOutContext = getApiOutContext(inputRefId,CERTIFICATE_RETRIEVED_SUCCESSFULLY,
                        messages.get(CERTIFICATE_RETRIEVED_SUCCESSFULLY));
                apiOutContext.setStatus(messages.get(SUCCESS_CODE));
            } else{
                apiOutContext = getApiOutContext(inputRefId,CERTIFICATE_IS_NOT_AVAILABLE,
                        messages.get(CERTIFICATE_IS_NOT_AVAILABLE));
                apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId,exception.getCode(), messages.get(exception.getCode()));
            apiOutContext.setStatus(messages.get(AppErrorConstants.FAILED_CODE));
        }
        certificateInfo.setApiOutContext(apiOutContext);
        return certificateInfo;
    }
}
