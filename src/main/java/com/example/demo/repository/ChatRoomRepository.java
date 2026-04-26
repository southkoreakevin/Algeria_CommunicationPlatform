package com.example.demo.repository;

import com.example.demo.domain.ChatRoom;

import java.util.Optional;

public interface ChatRoomRepository {
    ChatRoom save(ChatRoom chatRoom);
    Optional<ChatRoom> findById(Long id);
}