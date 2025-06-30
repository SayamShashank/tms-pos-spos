package com.ina.parameters.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ina.common.model.CommonResponse;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParamChecksumResponse extends CommonResponse {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 7845632902182753589L;
    private String paramChecksum;
}
