package org.shelterconnect.api.platform.security;

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
        // 브라우저에서도 사용할 수 있는 HTTP Basic 인증을 사용하므로 CSRF 보호를 유지합니다.
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
