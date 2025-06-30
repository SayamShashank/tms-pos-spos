package com.ina.certificates.controller;

import com.ina.certificates.service.InitialiseCertificateService;
import com.ina.common.crypto.model.certs.*;
import com.ina.common.model.ApiOutContext;
import com.ina.common.model.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.ina.CommonObjects.getApiInContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;




@ExtendWith(MockitoExtension.class)
class CertsControllerTest   {

    @InjectMocks
    private CertsController certsController;

    @Mock
    InitialiseCertificateService initialiseCertificateService;

    @BeforeEach
    void setup(){
        certsController=new CertsController(initialiseCertificateService);

    }

    @Test
    void contextLoads(){
        assertNotNull(certsController);
    }

    @Test
    void getServerCertsApiTest() {

        Request request = new Request();
        request.setApiInContext(getApiInContext());
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        ServerCertsResponse serverCertsResponse = new ServerCertsResponse();
        serverCertsResponse.setApiOutContext(apiOutContext);
        when(initialiseCertificateService.getAllServerCerts(request)).thenReturn(serverCertsResponse);
        ServerCertsResponse response = certsController.getServerCerts(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(), response.getApiOutContext().getOutputRefId());
    }

    @Test
    void getServerCertsWithStatusTest() {

        InitialiseCertRequest request = new InitialiseCertRequest();
        request.setApiInContext(getApiInContext());
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        ServerCertsStatusResponse serverCertsStatusResponse = new ServerCertsStatusResponse();
        serverCertsStatusResponse.setApiOutContext(apiOutContext);
        when(initialiseCertificateService.getServerCertificateStatus(request)).thenReturn(serverCertsStatusResponse);
        ServerCertsStatusResponse response = certsController.getServerCertsWithStatus(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(), response.getApiOutContext().getOutputRefId());
    }

    @Test
    void generateServerL4Certs(){

        InitialiseCertRequest request = new InitialiseCertRequest();
        request.setApiInContext(getApiInContext());
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        ServerCertsStatusResponse serverCertsStatusResponse = new ServerCertsStatusResponse();
        serverCertsStatusResponse.setApiOutContext(apiOutContext);
        when(initialiseCertificateService.generateL4Certs(request))
                .thenReturn(serverCertsStatusResponse);
        ServerCertsStatusResponse response = (ServerCertsStatusResponse) certsController.generateServerL4Certs(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(), response.getApiOutContext().getOutputRefId());

    }
    @Test
    void viewCertInfo(){
        InitialiseCertRequest request = new InitialiseCertRequest();
        request.setApiInContext(getApiInContext());
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        ViewCertificateInfo viewCertificateInfo=new ViewCertificateInfo();
        viewCertificateInfo.setApiOutContext(apiOutContext);
        when(initialiseCertificateService.viewCertInfo(request))
                .thenReturn(viewCertificateInfo);
        ViewCertificateInfo response = certsController.viewCertInfo(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(), response.getApiOutContext().getOutputRefId());

    }

    @Test
    void initialiseCertsTestApi(){
        InitialiseCertRequest request = new InitialiseCertRequest();
        request.setApiInContext(getApiInContext());
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        ServerCertsStatusResponse serverCertsStatusResponse = new ServerCertsStatusResponse();
        serverCertsStatusResponse.setApiOutContext(apiOutContext);
        when(initialiseCertificateService.generateServerCerts(request)).thenReturn(serverCertsStatusResponse);
        ServerCertsStatusResponse response = (ServerCertsStatusResponse) certsController.initialiseCerts(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(), response.getApiOutContext().getOutputRefId());

    }

    @Test
    void generateRootCertificateTestApi(){
        PublishRootCertificateRequest request = new PublishRootCertificateRequest();
        request.setApiInContext(getApiInContext());
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        ServerCertsStatusResponse serverCertsStatusResponse = new ServerCertsStatusResponse();
        serverCertsStatusResponse.setApiOutContext(apiOutContext);
        when(initialiseCertificateService.generateRootCertificate(request)).thenReturn(serverCertsStatusResponse);
        ServerCertsStatusResponse response = (ServerCertsStatusResponse) certsController.generateRootCertificate(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(), response.getApiOutContext().getOutputRefId());


    }
}



