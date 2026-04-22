package com.example.demo.repository.jpa;

import com.example.demo.domain.Friendship;
import com.example.demo.domain.FriendshipStatus;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataFriendshipRepository extends JpaRepository<Friendship, Long> {
    boolean existsByRequesterAndReceiver(User requester, User receiver);

    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user OR f.receiver = :user) AND f.status = :status")
    List<Friendship> findAllByUserAndStatus(@Param("user") User user, @Param("status") FriendshipStatus status);

    List<Friendship> findAllByReceiverAndStatus(User receiver, FriendshipStatus status);
}