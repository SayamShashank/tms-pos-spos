package com.ina.parameters.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ina.common.model.ApiOutContext;
import com.ina.parameters.model.GetParametersRequest;
import com.ina.parameters.model.ParameterSecureResponse;
import com.ina.parameters.service.GetParamChecksumService;
import com.ina.parameters.service.GetParametersService;
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
class ParameterControllerTest {

    @InjectMocks
    private ParameterController parameterController;

    @Mock
    GetParametersService getParametersService;
    @Mock
    GetParamChecksumService getParamChecksumService;

    @BeforeEach
    void setup(){
        parameterController=new ParameterController(getParametersService,getParamChecksumService);

    }

    @Test
    void contextLoads(){
        assertNotNull(parameterController);
    }

    @Test
    void getParameters() throws JsonProcessingException {
        GetParametersRequest request = new GetParametersRequest();
        request.setApiInContext(getApiInContext());
        ApiOutContext apiOutContext = new ApiOutContext();
        apiOutContext.setOutputRefId(request.getApiInContext().getInputRefId());
        ParameterSecureResponse parameterSecureResponse = new ParameterSecureResponse();
        parameterSecureResponse.setApiOutContext(apiOutContext);
        when(getParametersService.getParameters(request)).thenReturn(parameterSecureResponse);
        ParameterSecureResponse response = parameterController.getParameters(request);
        assertNotNull(response);
        assertEquals(request.getApiInContext().getInputRefId(), response.getApiOutContext().getOutputRefId());

    }
}