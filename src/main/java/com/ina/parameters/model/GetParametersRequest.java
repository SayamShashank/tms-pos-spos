package com.ina.parameters.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ina.common.model.CommonRequest;
import com.ina.common.model.SecureReqMetadata;
import jakarta.validation.Valid;
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
public class GetParametersRequest extends CommonRequest {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 5178342096753812649L;
    @Order(3)
    @NotNull(message = "secureMetadata cannot be null")
    @Valid
    private SecureReqMetadata secureReqMetadata;
}
