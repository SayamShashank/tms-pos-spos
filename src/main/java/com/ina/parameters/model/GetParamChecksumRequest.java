package com.ina.parameters.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ina.common.model.CommonRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;

@Getter
@Setter
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetParamChecksumRequest extends CommonRequest {
    @Serial
    private static final long serialVersionUID = 5178024396753812649L;
    @NotBlank(message = "mid cannot be blank")
    @NotNull(message = "mid cannot be null")
    @Valid
    private String mid;
    @NotBlank(message = "tid cannot be blank")
    @NotNull(message = "tid cannot be null")
    @Valid
    private String tid;
    @Order(7)
    @NotBlank(message = "trsMid cannot be blank")
    @NotNull(message = "trsMid cannot be null")
    @Valid
    private String trsMid;
}
