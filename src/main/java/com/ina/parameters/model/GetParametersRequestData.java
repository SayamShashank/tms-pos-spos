package com.ina.parameters.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ina.common.model.CommonRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;

@Getter
@Setter
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetParametersRequestData extends CommonRequest {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 5178342096753812649L;
    private ParameterRequestData requestData;
}
