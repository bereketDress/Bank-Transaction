package com.bankofcli.service.impl;

import com.bankofcli.exception.BankException;
import com.bankofcli.model.User;
import com.bankofcli.repository.contract.UserRepository;
import com.bankofcli.service.contract.UserService;
import com.bankofcli.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserServiceImpl(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public User registerUser(String name, String email, String password) {

        if (name == null || name.isBlank()) {
            throw new BankException("Name is required");
        }

        if (email == null || email.isBlank()) {
            throw new BankException("Email is required");
        }

        if (password == null || password.length() < 8) {
            throw new BankException("Password must be at least 8 characters");
        }

        User user = new User(
                null,
                name,
                email,
                passwordHasher.hash(password)
        );

        User savedUser = userRepository.save(user);
        logger.info("User registered successfully");
        return savedUser;
    }

    @Override
    public User loginUser(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BankException("User not found"));

        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new BankException("Incorrect email or password");
        }

        logger.info("User logged in successfully");
        return user;
    }

    @Override
    public User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BankException("User not found"));
    }
}
