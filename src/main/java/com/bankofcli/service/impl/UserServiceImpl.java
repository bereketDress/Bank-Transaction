package com.bankofcli.service.impl;

import com.bankofcli.exception.BankException;
import com.bankofcli.model.User;
import com.bankofcli.repository.UserRepository;
import com.bankofcli.service.SystemLogService;
import com.bankofcli.service.UserService;
import com.bankofcli.util.PasswordHasher;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public class UserServiceImpl implements UserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final SystemLogService systemLogService;

    public UserServiceImpl(UserRepository userRepository, PasswordHasher passwordHasher, SystemLogService systemLogService) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.systemLogService = Objects.requireNonNull(systemLogService);
    }

    @Override
    public User registerUser(String name, String email, String password) {
        String cleanName = requireText(name, "Name");
        String cleanEmail = requireText(email, "Email").toLowerCase(Locale.ROOT);
        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw new BankException("Enter a valid email address.");
        }
        if (password == null || password.length() < 8) {
            throw new BankException("Password must contain at least 8 characters.");
        }
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new BankException("A user with email " + cleanEmail + " already exists.");
        }

        User user = userRepository.save(
                new User(null, cleanName, cleanEmail, passwordHasher.hash(password)));
        systemLogService.info(user.getUserId(), "User registered successfully");
        return user;
    }

    @Override
    public User loginUser(String email, String password) {
        String cleanEmail = requireText(email, "Email").toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new BankException("User " + cleanEmail + " was not found."));
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            systemLogService.error(user.getUserId(), "Failed user login attempt");
            throw new BankException("Incorrect email or password.");
        }
        systemLogService.info(user.getUserId(), "User logged in successfully");
        return user;
    }

    @Override
    public User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BankException("User " + userId + " was not found."));
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BankException(fieldName + " is required.");
        }
        return value.trim();
    }
}
