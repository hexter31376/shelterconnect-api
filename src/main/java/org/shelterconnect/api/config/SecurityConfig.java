package org.shelterconnect.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            @Value("${springdoc.api-docs.enabled:false}") boolean docsEnabled) throws Exception {
        http.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers("/actuator/health", "/error").permitAll();
            if (docsEnabled) {
                authorize.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll();
            }
            authorize.anyRequest().authenticated();
        });
        // Retain CSRF protection while browser-capable HTTP Basic authentication is used.
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
