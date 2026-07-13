package com.brendo.queue.service;

import com.brendo.queue.dto.request.CreateUserRequest;
import com.brendo.queue.dto.request.UpdateUserRequest;
import com.brendo.queue.dto.response.UserResponse;
import com.brendo.queue.entity.User;
import com.brendo.queue.exception.ResourceAlreadyExistsException;
import com.brendo.queue.exception.ResourceNotFoundException;
import com.brendo.queue.repository.CounterRepository;
import com.brendo.queue.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CounterRepository counterRepository = mock(CounterRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final UserService userService = new UserService(userRepository, counterRepository, passwordEncoder);

    @Test
    void listFiltersUsersByRoleWhenProvided() {
        when(userRepository.findAllByRole("ATENDENTE"))
            .thenReturn(List.of(new User("attendant", "hash", "ATENDENTE")));

        List<UserResponse> response = userService.list(" atendente ");

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().username()).isEqualTo("attendant");
        assertThat(response.getFirst().role()).isEqualTo("ATENDENTE");
        verify(userRepository).findAllByRole("ATENDENTE");
    }

    @Test
    void createEncodesPasswordAndAssignsCounter() {
        when(userRepository.existsByUsername("attendant")).thenReturn(false);
        when(counterRepository.existsById(1L)).thenReturn(true);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.create(
            new CreateUserRequest(" attendant ", "password123", "atendente", 1L)
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(response.username()).isEqualTo("attendant");
        assertThat(response.role()).isEqualTo("ATENDENTE");
        assertThat(response.counterId()).isEqualTo(1L);
    }

    @Test
    void createThrowsWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThatThrownBy(() -> userService.create(new CreateUserRequest("admin", "password123", "ADMIN", null)))
            .isInstanceOf(ResourceAlreadyExistsException.class)
            .hasMessage("User already exists: admin");
    }

    @Test
    void updateChangesProfileAndPasswordWhenProvided() {
        User user = new User("old", "old-password", "ATENDENTE");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByUsernameAndIdNot("new", 1L)).thenReturn(false);
        when(passwordEncoder.encode("newpass123")).thenReturn("new-password");

        UserResponse response = userService.update(
            1L,
            new UpdateUserRequest("new", "newpass123", "ADMIN", null)
        );

        assertThat(response.username()).isEqualTo("new");
        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(user.getPasswordHash()).isEqualTo("new-password");
    }

    @Test
    void updateThrowsWhenCounterDoesNotExist() {
        User user = new User("attendant", "hash", "ATENDENTE");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(counterRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.update(
                1L,
                new UpdateUserRequest("attendant", null, "ATENDENTE", 99L)
            ))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Counter not found: 99");
    }
}
