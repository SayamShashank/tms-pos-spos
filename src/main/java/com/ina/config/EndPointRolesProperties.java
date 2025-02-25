package com.ina.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
@Data
public class EndPointRolesProperties {

    private final ObjectMapper objectMapper;
    private final List<RoleEndpoints> roles;

    @Autowired
    public EndPointRolesProperties(ObjectMapper objectMapper, @Value("${application.roles:}") String rolesJson) throws IOException {
        this.objectMapper = objectMapper;
        this.roles = (rolesJson == null || rolesJson.trim().isEmpty())
                ? new ArrayList<>()
                : objectMapper.readValue(rolesJson, new TypeReference<>() {});
    }

    @Data
    public static class RoleEndpoints {
        private String role;
        private List<String> endpoints;
    }
}
