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
public class TranslationKeys implements Serializable {

	private transient KeysMetadataResponse pin;
	
	private transient KeysMetadataResponse dataKey;
	
	private transient KeysMetadataResponse mac;

}
