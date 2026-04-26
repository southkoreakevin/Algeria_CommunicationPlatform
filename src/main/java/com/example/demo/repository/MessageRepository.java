package com.example.demo.repository;

import com.example.demo.domain.Message;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageRepository {
    void save(Message message);
    List<Message> findByChatRoomId(Long chatRoomId, Pageable pageable);
}