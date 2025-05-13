package com.ina.parameters.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ina.common.model.CommonResponse;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParameterResponse extends CommonResponse {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 7845632921082753589L;
    private transient TmsParams emvParameters;

}
