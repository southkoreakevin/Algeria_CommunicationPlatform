package com.example.demo.repository;

import com.example.demo.domain.Friendship;
import com.example.demo.domain.FriendshipStatus;
import com.example.demo.domain.User;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository {
    void save(Friendship friendship);
    Optional<Friendship> findById(Long id);
    boolean existsByRequesterAndReceiver(User requester, User receiver);
    List<Friendship> findAllByUserAndStatus(User user, FriendshipStatus status);
    List<Friendship> findAllByReceiverAndStatus(User receiver, FriendshipStatus status);
    void delete(Friendship friendship);
}