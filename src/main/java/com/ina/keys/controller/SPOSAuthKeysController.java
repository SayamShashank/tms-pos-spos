package com.ina.keys.controller;


import com.ina.common.crypto.model.keys.FetchSPOSAuthKeyRequest;
import com.ina.common.crypto.model.keys.SPOSAuthKeyRequest;
import com.ina.common.model.CommonResponse;
import com.ina.keys.service.SPOSAuthKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
public class SPOSAuthKeysController {

    private final SPOSAuthKeyService sposAuthKeyService;

    @PostMapping(GENERATE_SPOS_AUTH_KEY)
        public CommonResponse generateSPOSAuthKeys(@RequestBody SPOSAuthKeyRequest sposAuthKeyRequest){
        return sposAuthKeyService.generateSPOSAuthKey(sposAuthKeyRequest);
    }

    @PostMapping(GET_ALL_SPOS_AUTH_KEYS)
    public CommonResponse getAllSPOSKeys(@RequestBody FetchSPOSAuthKeyRequest request) {
        return sposAuthKeyService.getSPOSAuthKey(request);
    }

}
