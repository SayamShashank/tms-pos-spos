package com.ina.keys.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ina.common.model.CommonRequest;
import com.ina.common.model.SecureReqMetadata;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Validated
@Builder
@JsonInclude(Include.NON_NULL)
public class GetTranslationKeysRequest extends CommonRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9104443031941160805L;

	@NotNull(message = "secureMetaData cannot be null or empty")
	private SecureReqMetadata secureMetaData;

}
