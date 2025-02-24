package com.ina.keys.controller;


import com.ina.common.crypto.model.aekdek.AvailableServerKeysResponse;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.Request;
import com.ina.keys.service.AEKAndDEKKeysService;
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
@RequestMapping(value = SERVER_CONTEXT_FOR_KEYS + API + VERSION,
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor
public class AEKAndDEKKeysController {

	private final AEKAndDEKKeysService aekAndDEKKeysService;
	
	@PostMapping(GENERATE_DEK_AND_AEK_KEY)
	public CommonResponse generateDEKAndAEKKey(@RequestBody @Validated Request request) {
		return aekAndDEKKeysService.generateDEKAndAEKKey(request);
	}

	@PostMapping(ROTATE_DEK_AND_AEK_KEY)
	public CommonResponse rotateDEKAndAEKKey(@RequestBody @Validated Request request) {
		return aekAndDEKKeysService.rotateDEK(request);
	}

	@PostMapping(GET_ALL_AEK_DEK_SERVER_KEYS)
	public AvailableServerKeysResponse getAllAEKAndDEkServerKeys(@RequestBody @Validated Request request) {
		return aekAndDEKKeysService.getAllAEKAndDEkServerKeys(request);
	}

}
