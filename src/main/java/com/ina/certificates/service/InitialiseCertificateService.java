package com.ina.certificates.service;


import com.ina.common.crypto.model.certs.*;
import com.ina.common.crypto.service.CertGenerationService;
import com.ina.common.crypto.service.ServerEncryptionAndSignatureCertificateService;
import com.ina.common.enums.NextCommandDetails;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.*;
import com.ina.common.response.message.InaPayMessages;
import com.ina.common.validator.CommonValidator;
import com.ina.common.validator.DeviceProfileValidator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import static com.ina.common.constants.AppErrorConstants.*;
import static com.ina.common.utils.CommonUtils.getApiOutContext;
import static com.ina.common.utils.CommonUtils.throwValidationException;
import static com.ina.constants.AppConstants.TMS;
import static java.util.Objects.nonNull;

@Service
@Slf4j
public class InitialiseCertificateService extends CommonValidator<ApiInContext> {


    private final CertGenerationService certGenerationService;

    private final DeviceProfileValidator deviceProfileValidator;

    private final ServerEncryptionAndSignatureCertificateService serverEncryptionAndSignatureCertificateService;

    protected InitialiseCertificateService(InaPayMessages messages,
                                           CertGenerationService certGenerationService,
                                           ServerEncryptionAndSignatureCertificateService serverEncryptionAndSignatureCertificateService,
                                           DeviceProfileValidator deviceProfileValidator) {
        super(messages);
        this.certGenerationService = certGenerationService;
        this.serverEncryptionAndSignatureCertificateService = serverEncryptionAndSignatureCertificateService;
        this.deviceProfileValidator=deviceProfileValidator;
    }

    @Override
    public void evaluate(ApiInContext apiInContext) throws CommonValidationException {
        String inputRefId= apiInContext.getInputRefId();

        if (nonNull(apiInContext.getTimeStamp())){
            throw throwValidationException(inputRefId, TIME_STAMP_IS_NOT_AVAILABLE_IN_REQUEST,
                    messages, NextCommandDetails.BLOCK);
        }
//        deviceProfileValidator.timeStampFreshnessCheck(inputRefId, apiInContext.getTimeStamp());

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
                apiOutContext = getApiOutContext(inputRefId, CERTIFICATE_GENERATION_IS_SUCCESSFUL, messages, messages.get(SUCCESS_CODE));

            } else{
                apiOutContext = getApiOutContext(inputRefId, CERTIFICATE_GENERATION_FAILED, messages, messages.get(FAILED_CODE));

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
                apiOutContext = getApiOutContext(inputRefId,ROOT_CA_GENERATION_IS_SUCCESSFUL, messages, messages.get(SUCCESS_CODE));
            } else{
                apiOutContext = getApiOutContext(inputRefId,ROOT_CA_GENERATION_IS_FAILED, messages, messages.get(FAILED_CODE));
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
                apiOutContext = getApiOutContext(inputRefId,CERTIFICATE_GENERATION_FAILED, messages, messages.get(FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId,exception.getCode(),exception.getMessage(), messages.get(FAILED_CODE));

        }

        response.setApiOutContext(apiOutContext);
        return response;
    }


    public ServerCertsResponse getAllServerCerts(Request request) {
        ServerCertsResponse allServerCerts = certGenerationService.getAllServerCerts();
        ApiOutContext apiOutContext =  getApiOutContext(request.getApiInContext().getInputRefId()
                , SUCCESS_CODE,messages.get(SUCCESS_CODE), TMS);
        allServerCerts.setApiOutContext(apiOutContext);
        return allServerCerts;
    }

    public DeviceCertsResponse getDeviceCerts(CommonRequest request) {

        log.info("Started fetching all device certs....!");

        DeviceCertsResponse response = new DeviceCertsResponse();
        ApiOutContext apiOutContext;

        String deviceId = request.getDeviceMetadata().getDeviceId();
        String inputRefId = request.getApiInContext().getInputRefId();

        log.info("Started fetching all device certs for device Id : {}", deviceId);

        DeviceCertsResponse deviceCertsResponse = certGenerationService
                .getAllDeviceSpecificCerts(deviceId, inputRefId);

        if (CollectionUtils.isNotEmpty(deviceCertsResponse.getDeviceCerts())) {
            log.info("Device certs are not empty...!");
            apiOutContext = getApiOutContext(inputRefId, REQUESTED_CERTIFICATE_RETRIEVED_SUCCESSFULLY,
                    messages, messages.get(SUCCESS_CODE));
            response.setDeviceCerts(deviceCertsResponse.getDeviceCerts());
        } else {
            log.info("Device certs are empty...!");
            apiOutContext = getApiOutContext(inputRefId, REQUESTED_CERTIFICATE_IS_NOT_AVAILABLE,
                    messages, messages.get(FAILED_CODE));
            apiOutContext.setStatus(messages.get(FAILED_CODE));
        }

        response.setApiOutContext(apiOutContext);
        log.info("Fetching device certs is completed...!");
        return response;
    }

    public ServerCertsStatusResponse getServerCertificateStatus(InitialiseCertRequest certRequest) {
        ServerCertsStatusResponse serverCertificateStatus =
                certGenerationService.getServerCertificateStatus(certRequest.getCertificateType());
        ApiOutContext apiOutContext =  getApiOutContext(certRequest.getApiInContext().getInputRefId(), SUCCESS_CODE,
        messages.get(SUCCESS_CODE), TMS);
        serverCertificateStatus.setApiOutContext(apiOutContext);
        return serverCertificateStatus;
    }

    public ViewCertificateInfo viewCertInfo(InitialiseCertRequest certRequest) {

        ApiOutContext apiOutContext;
        ViewCertificateInfo certificateInfo = new ViewCertificateInfo();
        String inputRefId = certRequest.getApiInContext().getInputRefId();
        try {
            ViewCertificateInfo response = certGenerationService.
                    viewCertificate(certRequest.getCertificateType(), inputRefId);
            if(nonNull(response)){
                apiOutContext = getApiOutContext(inputRefId,REQUESTED_CERTIFICATE_RETRIEVED_SUCCESSFULLY,
                        messages, messages.get(SUCCESS_CODE));
                certificateInfo.setCertType(response.getCertType());
                certificateInfo.setCertContent(response.getCertContent());
                certificateInfo.setEnteredBy(response.getEnteredBy());
                certificateInfo.setEntryDate(response.getEntryDate());
                certificateInfo.setCertSerialNumber(response.getCertSerialNumber());
                certificateInfo.setKeyExpiry(response.getKeyExpiry());

            } else{
                apiOutContext = getApiOutContext(inputRefId,REQUESTED_CERTIFICATE_IS_NOT_AVAILABLE,
                        messages, messages.get(FAILED_CODE));
            }
        } catch (CommonValidationException exception) {
            apiOutContext = getApiOutContext(inputRefId,exception.getCode(), exception.getMessage(), messages.get(FAILED_CODE));
        }
        certificateInfo.setApiOutContext(apiOutContext);
        return certificateInfo;
    }
}
