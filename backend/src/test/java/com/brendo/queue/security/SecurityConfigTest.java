package com.brendo.queue.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void corsConfigurationUsesConfiguredOriginPatterns() {
        SecurityConfig securityConfig = new SecurityConfig(
            mock(JwtAuthFilter.class),
            "http://localhost:4200, https://*.example.com "
        );

        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/tickets");
        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins()).isNull();
        assertThat(configuration.getAllowedOriginPatterns())
            .containsExactly("http://localhost:4200", "https://*.example.com");
        assertThat(configuration.getAllowedMethods())
            .containsExactlyElementsOf(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        assertThat(configuration.getAllowCredentials()).isTrue();
    }
}
