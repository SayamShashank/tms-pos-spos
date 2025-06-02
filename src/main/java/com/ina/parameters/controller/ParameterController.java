package com.ina.parameters.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ina.parameters.model.GetParametersRequest;
import com.ina.parameters.model.ParameterSecureResponse;
import com.ina.parameters.service.GetParametersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ina.constants.ApiEndpoints.*;

@RestController
@RequestMapping(value = SERVER_CONTEXT_FOR_TMS + API + VERSION+PARAMETERS ,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class ParameterController {

    private final GetParametersService getParametersService;

    public ParameterController(GetParametersService getParametersService) {
        this.getParametersService = getParametersService;
    }
    @PostMapping("getParameters")
    public ParameterSecureResponse getParameters(@RequestBody @Validated GetParametersRequest request) throws JsonProcessingException {
        log.info("Inside getParameters method");
        return getParametersService.getParameters(request);
    }




}
