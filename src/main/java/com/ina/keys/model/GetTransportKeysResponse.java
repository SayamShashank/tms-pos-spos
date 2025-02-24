package com.ina.keys.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ina.common.model.CommonResponse;
import com.ina.common.model.SecureRespMetadata;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class GetTransportKeysResponse extends CommonResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8675272585342679772L;

	private SecureRespMetadata secureMetaData;

}
