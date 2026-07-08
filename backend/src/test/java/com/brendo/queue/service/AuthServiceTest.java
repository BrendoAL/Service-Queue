package com.brendo.queue.service;

import com.brendo.queue.dto.request.LoginRequest;
import com.brendo.queue.dto.response.LoginResponse;
import com.brendo.queue.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final AuthService authService = new AuthService(authenticationManager, jwtTokenProvider, 28800000);

    @Test
    void loginAuthenticatesUserAndReturnsJwtResponse() {
        User principal = new User(
            "admin",
            "password-hash",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtTokenProvider.generateToken("admin", "ADMIN")).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("admin", "secret"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInMs()).isEqualTo(28800000);
        assertThat(response.username()).isEqualTo("admin");
        assertThat(response.role()).isEqualTo("ADMIN");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken("admin", "ADMIN");
    }
}
