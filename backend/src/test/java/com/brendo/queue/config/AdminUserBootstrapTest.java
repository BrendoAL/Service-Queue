package com.brendo.queue.config;

import com.brendo.queue.entity.User;
import com.brendo.queue.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminUserBootstrapTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    @Test
    void createsAdminUserWhenPasswordIsConfiguredAndUserDoesNotExist() {
        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");

        AdminUserBootstrap bootstrap =
            new AdminUserBootstrap(userRepository, passwordEncoder, "admin", "secret");

        bootstrap.run(null);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void skipsAdminCreationWhenPasswordIsMissing() {
        AdminUserBootstrap bootstrap =
            new AdminUserBootstrap(userRepository, passwordEncoder, "admin", "");

        bootstrap.run(null);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void skipsAdminCreationWhenUserAlreadyExists() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        AdminUserBootstrap bootstrap =
            new AdminUserBootstrap(userRepository, passwordEncoder, "admin", "secret");

        bootstrap.run(null);

        verify(userRepository, never()).save(any(User.class));
    }
}
