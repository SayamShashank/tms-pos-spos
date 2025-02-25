package com.ina.certificates.controller;

import com.ina.certificates.service.InitialiseCertificateService;
import com.ina.common.crypto.model.certs.*;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ina.constants.ApiEndpoints.*;

@RestController
@RequestMapping(value = SERVER_CONTEXT_FOR_TMS + API + VERSION + DEVICE_CERTS,
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class CertsController {

	private final InitialiseCertificateService initialiseCertificateService;

	protected CertsController(InitialiseCertificateService initialiseCertificateService) {
        this.initialiseCertificateService = initialiseCertificateService;
    }

	@PostMapping(GENERATE_SERVER_CERTS)
	public CommonResponse initialiseCerts(@RequestBody InitialiseCertRequest certRequest){
		return initialiseCertificateService.generateServerCerts(certRequest);
	}

	@PostMapping(GENERATE_ROOT_CERT)
	public CommonResponse generateRootCertificate(@RequestBody PublishRootCertificateRequest request){
		return initialiseCertificateService.generateRootCertificate(request);
	}

	@PostMapping(GENERATE_SERVER_L4_CERTS)
	public CommonResponse generateServerL4Certs(@RequestBody InitialiseCertRequest certRequest){
		return initialiseCertificateService.generateL4Certs(certRequest);
	}

	@PostMapping(GET_ALL_SERVER_CERTS)
	public ServerCertsResponse getServerCerts(@RequestBody Request request) {
		return initialiseCertificateService.getAllServerCerts(request);
	}

	@PostMapping(GET_SERVER_CERTS_WITH_STATUS)
	public ServerCertsStatusResponse getServerCertsWithStatus(@RequestBody InitialiseCertRequest certRequest) {
		return initialiseCertificateService.getServerCertificateStatus(certRequest);
	}

	@PostMapping(VIEW_CERT_INFO)
	public ViewCertificateInfo viewCertInfo(@RequestBody InitialiseCertRequest certRequest) {
		return initialiseCertificateService.viewCertInfo(certRequest);
	}
}
