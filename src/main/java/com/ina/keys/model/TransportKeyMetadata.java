package com.ina.keys.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
public class TransportKeyMetadata implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6474478557307066565L;
	
	private String hostEphemeralPublicKey;
	
	private String transportKCV;
}
