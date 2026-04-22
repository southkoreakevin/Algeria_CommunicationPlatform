package com.example.demo.repository.jpa;

import com.example.demo.domain.Friendship;
import com.example.demo.domain.FriendshipStatus;
import com.example.demo.domain.User;
import com.example.demo.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaFriendshipRepository implements FriendshipRepository {

    private final SpringDataFriendshipRepository springDataFriendshipRepository;

    @Override
    public void save(Friendship friendship) {
        springDataFriendshipRepository.save(friendship);
    }

    @Override
    public Optional<Friendship> findById(Long id) {
        return springDataFriendshipRepository.findById(id);
    }

    @Override
    public boolean existsByRequesterAndReceiver(User requester, User receiver) {
        return springDataFriendshipRepository.existsByRequesterAndReceiver(requester, receiver);
    }

    @Override
    public List<Friendship> findAllByUserAndStatus(User user, FriendshipStatus status) {
        return springDataFriendshipRepository.findAllByUserAndStatus(user, status);
    }

    @Override
    public List<Friendship> findAllByReceiverAndStatus(User receiver, FriendshipStatus status) {
        return springDataFriendshipRepository.findAllByReceiverAndStatus(receiver, status);
    }

    @Override
    public void delete(Friendship friendship) {
        springDataFriendshipRepository.delete(friendship);
    }
}