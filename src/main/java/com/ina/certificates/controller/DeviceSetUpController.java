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
	@Operation(summary = "Setup Device Txn Service Init")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device setup Initialization is successfull",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = DeviceTMSInitResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "404", description = "Invalid request", content = @Content)})
	public DeviceTMSInitResponse deviceTXNInit(@io.swagger.v3.oas.annotations.parameters.RequestBody(
	        description = "Device setup Initialization - Device Certificates will be signed and return along with server certificates.", required = true,
	        content = @Content(mediaType = "application/json",
	            schema = @Schema(implementation = DeviceTMSInitRequest.class),
	            examples = @ExampleObject(value = "{ \"certCSRMetadata\":{} }")))
	     @RequestBody @Validated DeviceTMSInitRequest request) throws Exception {
		return deviceSetUpService.deviceTXNInit(request);
	}
}
