package com.brendo.queue.service;

import com.brendo.queue.dto.request.LoginRequest;
import com.brendo.queue.dto.response.LoginResponse;
import com.brendo.queue.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final long jwtExpirationMs;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            @Value("${jwt.expiration-ms}") long jwtExpirationMs) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String role = resolveRole(userDetails);
        String token = jwtTokenProvider.generateToken(userDetails.getUsername(), role);

        return new LoginResponse(token, "Bearer", jwtExpirationMs, userDetails.getUsername(), role);
    }

    private String resolveRole(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith("ROLE_"))
            .map(authority -> authority.substring("ROLE_".length()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Authenticated user has no role"));
    }
}
