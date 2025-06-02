package com.ina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan({"com.ina"})
@EntityScan({"com.ina.common.dao.entity", "com.ina.transaction.entity","com.ina.common.crypto.entity","com.ina.dao.entity"})
@EnableJpaRepositories({"com.ina.common.dao", "com.ina.transaction.repository","com.ina.common.crypto.repository","com.ina.dao"})
@SpringBootApplication(exclude =  {DataSourceAutoConfiguration.class })
public class InaPayTMSServiceApplication {
    public static void main(String[] args)  {

        SpringApplication.run(InaPayTMSServiceApplication.class, args);
    }


}
