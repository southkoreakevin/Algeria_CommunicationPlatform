package com.example.demo.repository.jpa;

import com.example.demo.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataChatRoomRepository extends JpaRepository<ChatRoom, Long> {}