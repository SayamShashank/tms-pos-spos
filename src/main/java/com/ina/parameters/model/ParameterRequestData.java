package com.ina.parameters.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

@Getter
@Setter
@Validated
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParameterRequestData implements Serializable {

    @Order(3)
    @NotBlank(message = "serialNo cannot be blank")
    @NotNull(message = "serialNo cannot be null")
    @Valid
    private String serialNo;

    @NotBlank(message = "tid cannot be blank")
    @NotNull(message = "tid cannot be null")
    @Valid
    private String tid;
    @Order(7)
    @NotBlank(message = "trsMid cannot be blank")
    @NotNull(message = "trsMid cannot be null")
    @Valid
    private String trsMid;
    @Order(8)
    @NotBlank(message = "keySerialNo cannot be blank")
    @NotNull(message = "keySerialNo cannot be null")
    @Valid
    private String keySerialNo;
    @Order(10)
    @NotBlank(message = "tmsChallenge cannot be blank")
    @NotNull(message = "tmsChallenge cannot be null")
    @Valid
    private String tmsChallenge;

}
