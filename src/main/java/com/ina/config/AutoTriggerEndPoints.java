package com.ina.config;

import com.ina.common.constants.AppErrorConstants;
import com.ina.common.crypto.model.aekdek.GenerateAEKAndDEKResponse;
import com.ina.common.crypto.service.AEKAndDEKService;
import com.ina.common.exception.CommonValidationException;
import com.ina.common.model.ApiOutContext;
import com.ina.common.model.CommonResponse;
import com.ina.common.response.message.InaPayMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AutoTriggerEndPoints {

    @Autowired
    AEKAndDEKService aekAndDEKService;

    private final InaPayMessages inaPayMessages;

    @Bean
    public ApplicationRunner runOnStartup() {
        return args -> {

            CommonResponse response = new CommonResponse();
            ApiOutContext apiOutContext = new ApiOutContext();
            GenerateAEKAndDEKResponse generateAEKAndDEKResponse = new GenerateAEKAndDEKResponse();
            try {
                generateAEKAndDEKResponse = aekAndDEKService.generateAEKAndDEK("TXN", UUID.randomUUID().toString());
                apiOutContext.setCode(AppErrorConstants.SUCCESS_CODE);
                apiOutContext.setMessage(inaPayMessages.get(AppErrorConstants.SUCCESS_CODE));
                apiOutContext.setStatus(inaPayMessages.get(AppErrorConstants.SUCCESS_CODE));
            } catch (CommonValidationException exception) {
                apiOutContext.setCode(exception.getCode());
                apiOutContext.setMessage(exception.getMessage());
                apiOutContext.setStatus(inaPayMessages.get(AppErrorConstants.FAILED_CODE));
            }
            response.setApiOutContext(apiOutContext);
            log.info("AEK & DEK Server Keys: {}", generateAEKAndDEKResponse.getGenerateAEKAndDEKInfos());
        };
    }
}
