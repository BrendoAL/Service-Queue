package com.brendo.queue.config;

import com.brendo.queue.entity.User;
import com.brendo.queue.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminUserBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminUserBootstrap(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${queue.admin.username:admin}") String adminUsername,
            @Value("${queue.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminPassword)) {
            logger.info("Initial admin user bootstrap skipped because QUEUE_ADMIN_PASSWORD is not configured");
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            logger.info("Initial admin user bootstrap skipped because user '{}' already exists", adminUsername);
            return;
        }

        User admin = new User(adminUsername, passwordEncoder.encode(adminPassword), "ADMIN");
        userRepository.save(admin);
        logger.info("Initial admin user '{}' created", adminUsername);
    }
}
