package com.example.demo.repository.memory;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryUserRepository implements UserRepository {

    private static Map<Long, User> store = new ConcurrentHashMap<>();
    private static long sequence = 0L;

    @Override
    public void save(User user) {
        user.assignId(++sequence);
        store.put(user.getId(), user);
    }

    @Override
    public boolean existByEmail(String email) {
        return store.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return store.values().stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }

    public void clearStore(){
        store.clear();
    }
}
