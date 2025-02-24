package com.ina.certificates.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ina.common.crypto.model.init.CertCSRMetadata;
import com.ina.common.model.CommonRequest;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@JsonInclude(Include.NON_NULL)
public class DeviceTMSInitRequest extends CommonRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9104443031941160805L;
	
	@NotNull(message = "keys meta data cannot be null or blank.")
	private CertCSRMetadata certCSRMetadata;

}
