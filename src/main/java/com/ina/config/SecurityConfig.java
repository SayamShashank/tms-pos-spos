package com.ina.config;

import com.ina.common.exception.JwtAuthenticationEntryPoint;
import com.ina.common.utils.CommonRoleUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class SecurityConfig {

    private final EndPointRolesProperties endPointRolesConfig;

    @Value("${keycloak.auth.issuer}")
    private String keycloakAuthIssuer;

    @Value("${keycloak.certs.url}")
    private String keycloakCertsUrl;

    @Value("${client.reference}")
    private String clientReference;

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private final CommonRoleUtils commonRoleUtils;

    public SecurityConfig(EndPointRolesProperties endPointRolesConfig, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, CommonRoleUtils commonRoleUtils) {
        this.endPointRolesConfig = endPointRolesConfig;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.commonRoleUtils = commonRoleUtils;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http.oauth2Login(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler()))
                .authorizeHttpRequests(auth ->
                    endPointRolesConfig.getRoles().forEach(roleEndpoint ->
                        roleEndpoint.getEndpoints().forEach(endpoint ->
                            auth.requestMatchers(endpoint).hasAuthority(roleEndpoint.getRole())
                        )
                    )
                )
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/swagger-ui/**", "/v2/api-docs/**","/v3/api-docs/**", "/swagger-resources/**", "/webjars/**")
                            .permitAll()
                            .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(keycloakAuthIssuer);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        return commonRoleUtils.getJwtAuthenticationConverterHandler(keycloakAuthIssuer, keycloakCertsUrl, clientReference);
    }

    @Bean
    public AccessDeniedHandler jwtAccessDeniedHandler() {
        return commonRoleUtils.jwtAccessDeniedException();
    }

}


