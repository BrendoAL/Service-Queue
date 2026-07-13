package com.brendo.queue.service;

import com.brendo.queue.dto.request.CreateUserRequest;
import com.brendo.queue.dto.request.UpdateUserRequest;
import com.brendo.queue.dto.response.UserResponse;
import com.brendo.queue.entity.User;
import com.brendo.queue.exception.ResourceAlreadyExistsException;
import com.brendo.queue.exception.ResourceNotFoundException;
import com.brendo.queue.repository.CounterRepository;
import com.brendo.queue.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CounterRepository counterRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            CounterRepository counterRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.counterRepository = counterRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(String role) {
        List<User> users = StringUtils.hasText(role)
            ? userRepository.findAllByRole(role.trim().toUpperCase())
            : userRepository.findAll();

        return users.stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("User already exists: " + username);
        }

        validateCounterExists(request.counterId());

        User user = new User(
            username,
            passwordEncoder.encode(request.password()),
            request.role().trim().toUpperCase()
        );
        user.setCounterId(request.counterId());

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = findById(id);
        String username = request.username().trim();

        if (userRepository.existsByUsernameAndIdNot(username, id)) {
            throw new ResourceAlreadyExistsException("User already exists: " + username);
        }

        validateCounterExists(request.counterId());
        user.updateProfile(username, request.role().trim().toUpperCase(), request.counterId());

        if (StringUtils.hasText(request.password())) {
            user.changePassword(passwordEncoder.encode(request.password()));
        }

        return toResponse(user);
    }

    private void validateCounterExists(Long counterId) {
        if (counterId != null && !counterRepository.existsById(counterId)) {
            throw new ResourceNotFoundException("Counter not found: " + counterId);
        }
    }

    private User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getRole(),
            user.getCounterId(),
            user.getCreatedAt()
        );
    }
}
