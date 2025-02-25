package com.ina.certificates.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ina.common.crypto.model.init.SignedCertMetadata;
import com.ina.common.model.CommonResponse;
import lombok.*;

import java.io.Serial;

@Getter
@Setter
@Builder
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTMSInitResponse extends CommonResponse {

	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = -8675272585342679772L;

	private SignedCertMetadata signedCertMetadata;

}
