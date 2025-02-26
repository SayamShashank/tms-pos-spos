package com.ina.certificates.controller;

import com.ina.certificates.model.DeviceTMSInitRequest;
import com.ina.certificates.model.DeviceTMSInitResponse;
import com.ina.certificates.service.DeviceSetUpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ina.constants.ApiEndpoints.*;

@RestController
@RequestMapping(value = SERVER_CONTEXT_FOR_TMS + API + VERSION + DEVICE_SETUP,
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class DeviceSetUpController {

	private final DeviceSetUpService deviceSetUpService;
	
	@PostMapping(SETUP_TMS_INIT)
	public DeviceTMSInitResponse deviceTMSInit(@RequestBody @Validated DeviceTMSInitRequest request) {
		return deviceSetUpService.deviceTMSInit(request);
	}
}
