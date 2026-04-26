package com.example.demo.repository.jpa;

import com.example.demo.domain.ChatRoom;
import com.example.demo.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaChatRoomRepository implements ChatRoomRepository {

    private final SpringDataChatRoomRepository springDataChatRoomRepository;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return springDataChatRoomRepository.save(chatRoom);
    }

    @Override
    public Optional<ChatRoom> findById(Long id) {
        return springDataChatRoomRepository.findById(id);
    }
}