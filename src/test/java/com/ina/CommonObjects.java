package com.ina;


import com.ina.certificates.model.DeviceTMSInitRequest;
import com.ina.certificates.model.DeviceTMSInitResponse;
import com.ina.common.crypto.model.aekdek.AvailableServerKeys;
import com.ina.common.crypto.model.aekdek.AvailableServerKeysResponse;
import com.ina.common.crypto.model.certs.CertificateGenerationResponse;
import com.ina.common.crypto.model.certs.InitialiseCertRequest;
import com.ina.common.crypto.model.certs.PublishRootCertificateRequest;
import com.ina.common.crypto.model.certs.RootCertificateResponse;
import com.ina.common.crypto.model.init.CertCSRMetadata;
import com.ina.common.crypto.model.init.SignedCertMetadata;
import com.ina.common.device.model.DeviceProfileBlockRequest;
import com.ina.common.device.model.DeviceUnblockRequest;
import com.ina.common.model.*;
import com.ina.tms.packages.xml.v8.catm318.Document;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class CommonObjects {


    public static Request buildRequest() {
        Request request = new Request();
        request.setApiInContext(getApiInContext());
        return request;
    }
    public static InitialiseCertRequest createValidRequest() {
        ApiInContext apiInContext = new ApiInContext();
        apiInContext.setInputRefId("ref123");
        apiInContext.setTimeStamp("2025-06-01T12:00:00Z");

        InitialiseCertRequest request = new InitialiseCertRequest();
        request.setApiInContext(apiInContext);
        request.setCertificateType("server");

        return request;
    }


    public static PublishRootCertificateRequest createValidRootCertRequest() {
        ApiInContext apiInContext = new ApiInContext();
        apiInContext.setInputRefId("rootRef123");

        RootCertificateResponse rootCert = new RootCertificateResponse();
        rootCert.setCert("certificate-data");
        rootCert.setSkLmk("sk-lmk-data");

        PublishRootCertificateRequest request = new PublishRootCertificateRequest();
        request.setApiInContext(apiInContext);
        request.setRootCertificateResponse(rootCert);

        return request;
    }




    public static CommonResponse commonResponse(Request request) {
        CommonResponse response = new CommonResponse();
        response.setApiOutContext(getApiOutContextData());
        return response;
    }

    public static ApiInContext getApiInContext() {
        ApiInContext apiInContext = new ApiInContext();
        apiInContext.setInputRefId("1234");
        apiInContext.setTimeStamp("2025-01-27T12:00:00");
        return apiInContext;
    }

    public static ApiOutContext getApiOutContextDataForCertificate() {
        ApiOutContext apiOutContext=new ApiOutContext();
        apiOutContext.setOutputRefId("ref123");
        apiOutContext.setTimeStamp("2025-01-27T12:00:00");
        apiOutContext.setStatus("9999");
        apiOutContext.setCode("0000");
        return  apiOutContext;

    }


    public static ApiOutContext getApiOutContextData(){
        ApiOutContext apiOutContext=new ApiOutContext();
        apiOutContext.setOutputRefId("1234");
        apiOutContext.setTimeStamp("2025-01-27T12:00:00");
        apiOutContext.setStatus("9999");
        apiOutContext.setCode("0000");
        return  apiOutContext;
    }
    public static  InitialiseCertRequest createRequest() {
        ApiInContext apiInContext = new ApiInContext();
        apiInContext.setInputRefId("ref123");
        apiInContext.setTimeStamp("2025-06-01T12:00:00Z");

        InitialiseCertRequest request = new InitialiseCertRequest();
        request.setApiInContext(apiInContext);
        request.setCertificateType("L4");

        return request;
    }


    public static DeviceUnblockRequest buildDeviceUnBlockRequest(){
        DeviceUnblockRequest deviceUnblockRequest=new DeviceUnblockRequest();
        deviceUnblockRequest.setStatus(true);
        deviceUnblockRequest.setAttestationBlock(true);
        deviceUnblockRequest.setThreatBlock(true);
        deviceUnblockRequest.setApiInContext(getApiInContext());
        deviceUnblockRequest.setDeviceMetadata(DeviceMetadata.builder().build());
        return deviceUnblockRequest;
    }


    public static DeviceProfileBlockRequest buildDeviceBlockRequest() {
        DeviceProfileBlockRequest deviceProfileBlockRequest=new DeviceProfileBlockRequest();
        deviceProfileBlockRequest.setApiInContext(getApiInContext());
        deviceProfileBlockRequest.setDeviceMetadata(DeviceMetadata.builder().build());
        deviceProfileBlockRequest.setBlockType(deviceProfileBlockRequest.getBlockType());
        deviceProfileBlockRequest.setAttestationBlock(true);
        deviceProfileBlockRequest.setThreatBlock(true);
        deviceProfileBlockRequest.setDeviceBlock(true);
        return deviceProfileBlockRequest;
    }
    public static CertificateGenerationResponse buildCertificateGenerationResponse(){
        CertificateGenerationResponse certificateGenerationResponse=new CertificateGenerationResponse();
        certificateGenerationResponse.setStatus("SUCCESS");
        certificateGenerationResponse.setCode("200");
        certificateGenerationResponse.setMessage("Certificate generated successfully");
        certificateGenerationResponse.setCert("-----BEGIN CERTIFICATE-----\nFAKE-CERTIFICATE-DATA-123456\n-----END CERTIFICATE-----");
        certificateGenerationResponse.setSkLmk("SKLMK-1234567890");
        return certificateGenerationResponse;
    }


    public static AvailableServerKeysResponse getDummyAvailableServerKeysResponse() {

        AvailableServerKeysResponse availableServerKeysResponse=new AvailableServerKeysResponse();
        availableServerKeysResponse.setApiOutContext(getApiOutContextData());
        AvailableServerKeys key1 = new AvailableServerKeys();
        key1.setKeyType("AEK");
        AvailableServerKeys key2 = new AvailableServerKeys();
        key2.setKeyType("DEK");
        List<AvailableServerKeys> keyList = Arrays.asList(key1, key2);
        availableServerKeysResponse.setAvailableServerKeysList(keyList);
        return availableServerKeysResponse;


    }






    public static DeviceTMSInitResponse buildDeviceTMSInitResponse(){
        DeviceTMSInitResponse deviceTMSInitResponse=new DeviceTMSInitResponse();
        deviceTMSInitResponse.setSignedCertMetadata(SignedCertMetadata.builder().build());
        deviceTMSInitResponse.setApiOutContext(commonResponse(new Request()).getApiOutContext());
        return deviceTMSInitResponse;
    }

    //Building the DeviceTMSInitRequest
    public static DeviceTMSInitRequest buildDeviceTMSInitRequest(){
        DeviceTMSInitRequest deviceTMSInitRequest=new DeviceTMSInitRequest();
        deviceTMSInitRequest.setDeviceMetadata(DeviceMetadata.builder().build());
        deviceTMSInitRequest.setApiInContext(getApiInContext());
        deviceTMSInitRequest.setCertCSRMetadata(CertCSRMetadata.builder().build());
        return deviceTMSInitRequest;
    }
    public static String readXmlAsString() throws IOException {
        ClassPathResource resource = new ClassPathResource("config.xml");
        String xmlToString = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        log.info("message: {}", xmlToString);
        return xmlToString;
    }

    public  Object maptoParamDocObject(String xmlData) {
        try {
            return unmarshal(xmlData, Document.class);
        } catch (Exception e) {
            log.error("Failed to read or unmarshal XML: {}", e.getMessage(), e);
            return null;
        }
    }
    public  com.ina.tms.packages.xml.v8.catm217.Document maptoRegisterDocObject(String xmlData) {
        try {
            return unmarshal(xmlData, com.ina.tms.packages.xml.v8.catm217.Document.class);
        } catch (Exception e) {
            log.error("Failed to read or unmarshal XML: {}", e.getMessage(), e);
            return null;
        }
    }

    private <T> T unmarshal(String xmlData, Class<T> targetClass) {
        if (xmlData == null || xmlData.isEmpty() || targetClass == null) {
            log.warn("unmarshal: invalid arguments");
            return null;
        }
        try {
            JAXBContext context = JAXBContext.newInstance(targetClass);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StreamSource source = new StreamSource(new StringReader(xmlData));
            JAXBElement<T> jaxbElement = unmarshaller.unmarshal(source, targetClass);
            return jaxbElement.getValue();
        } catch (JAXBException e) {
            log.error("Unmarshal failed for {}: {}", targetClass.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
      /* EMVParameters parameters = getEmvParameters(requestData);
            Gson gson = new Gson();
            Type aidListType = new TypeToken<List<AidList>>(){}.getType();
            List<AidList> aidList = gson.fromJson(parameters.getAids(), aidListType);
            Type ridListType = new TypeToken<List<RidList>>(){}.getType();
            List<RidList> ridList = gson.fromJson(parameters.getCpks(), ridListType);
            MerchantTerminalData merchantTerminalData = objectMapper.readValue(parameters.getTerminalConfig(), MerchantTerminalData.class);
            TmsParams tmsParams =new TmsParams();
            tmsParams .setCpks(ridList);
            tmsParams.setAids(aidList);
            tmsParams.setTerminalConfig(merchantTerminalData);*/







}


