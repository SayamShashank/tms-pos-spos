package com.ina.certificates.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.core.annotation.Order;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;

@Getter
@Setter
@Validated
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CertificateMetadata implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -70094405776546588L;

    @NotBlank(message = "data cannot be null or empty")
    @Order(2)
    private String data;

}
