package com.example.demo.repository;

import com.example.demo.domain.User;
import java.util.Optional;

public interface UserRepository {
    void save(User user);
    boolean existByEmail(String email);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
}
