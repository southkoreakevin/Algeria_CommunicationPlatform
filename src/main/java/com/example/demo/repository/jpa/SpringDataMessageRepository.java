package com.example.demo.repository.jpa;

import com.example.demo.domain.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataMessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatRoomIdOrderBySentAtDesc(Long chatRoomId, Pageable pageable);
}