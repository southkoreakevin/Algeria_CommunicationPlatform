package com.example.demo.repository;

import com.example.demo.domain.ChatRoom;
import com.example.demo.domain.ChatRoomMember;
import com.example.demo.domain.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository {
    void save(ChatRoomMember member);
    List<ChatRoomMember> findByUser(User user);
    List<ChatRoomMember> findByChatRoom(ChatRoom chatRoom);
    Optional<ChatRoom> findDirectRoomBetween(User user1, User user2);
    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);
    void deleteByChatRoomAndUser(ChatRoom chatRoom, User user);
}