package com.example.demo.repository.jpa;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Primary
@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public void save(User user) {
        springDataUserRepository.save(user);
    }

    @Override
    public boolean existByEmail(String email) {
        return springDataUserRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email);
    }
}