package com.example.demo.service;

import com.example.demo.domain.Friendship;
import com.example.demo.domain.FriendshipStatus;
import com.example.demo.domain.User;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.web.dto.FriendRequestResponse;
import com.example.demo.web.dto.FriendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService1 implements FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendRequest(String requesterEmail, String receiverEmail) {
        if (requesterEmail.equals(receiverEmail)) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        User requester = getUser(requesterEmail);
        User receiver = getUser(receiverEmail);

        if (friendshipRepository.existsByRequesterAndReceiver(requester, receiver) ||
                friendshipRepository.existsByRequesterAndReceiver(receiver, requester)) {
            throw new IllegalStateException("이미 친구이거나 요청이 존재합니다.");
        }

        friendshipRepository.save(new Friendship(requester, receiver));
        log.info("[FRIEND] 친구 요청 전송 - from: {} to: {}", requesterEmail, receiverEmail);
    }

    @Override
    @Transactional
    public void accept(String receiverEmail, Long friendshipId) {
        Friendship friendship = getFriendship(friendshipId);

        if (!friendship.getReceiver().getEmail().equals(receiverEmail)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("처리할 수 없는 요청입니다.");
        }

        friendship.accept();
        friendshipRepository.save(friendship);
        log.info("[FRIEND] 친구 요청 수락 - friendshipId: {}", friendshipId);
    }

    @Override
    @Transactional
    public void reject(String receiverEmail, Long friendshipId) {
        Friendship friendship = getFriendship(friendshipId);

        if (!friendship.getReceiver().getEmail().equals(receiverEmail)) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("처리할 수 없는 요청입니다.");
        }

        friendshipRepository.delete(friendship);
        log.info("[FRIEND] 친구 요청 거절 - friendshipId: {}", friendshipId);
    }

    @Override
    public List<FriendResponse> getFriends(String email) {
        User user = getUser(email);
        return friendshipRepository.findAllByUserAndStatus(user, FriendshipStatus.ACCEPTED)
                .stream()
                .map(f -> {
                    User friend = f.getRequester().getId().equals(user.getId())
                            ? f.getReceiver() : f.getRequester();
                    return new FriendResponse(friend.getId(), friend.getEmail());
                })
                .toList();
    }

    @Override
    public List<FriendRequestResponse> getPendingRequests(String email) {
        User user = getUser(email);
        return friendshipRepository.findAllByReceiverAndStatus(user, FriendshipStatus.PENDING)
                .stream()
                .map(f -> new FriendRequestResponse(f.getId(), f.getRequester().getEmail()))
                .toList();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다: " + email));
    }

    private Friendship getFriendship(Long id) {
        return friendshipRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 요청입니다."));
    }
}