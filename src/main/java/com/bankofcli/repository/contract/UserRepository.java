package com.bankofcli.repository;

import com.bankofcli.model.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(long userId);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
