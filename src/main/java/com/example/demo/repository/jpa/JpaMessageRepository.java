package com.example.demo.repository.jpa;

import com.example.demo.domain.Message;
import com.example.demo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaMessageRepository implements MessageRepository {

    private final SpringDataMessageRepository springDataMessageRepository;

    @Override
    public void save(Message message) {
        springDataMessageRepository.save(message);
    }

    @Override
    public List<Message> findByChatRoomId(Long chatRoomId, Pageable pageable) {
        return springDataMessageRepository.findByChatRoomIdOrderBySentAtDesc(chatRoomId, pageable);
    }

    @Override
    public long countByChatRoomIdAndIdGreaterThan(Long chatRoomId, Long lastReadMessageId) {
        return springDataMessageRepository.countByChatRoomIdAndIdGreaterThan(chatRoomId, lastReadMessageId);
    }

    @Override
    public long countByChatRoomId(Long chatRoomId) {
        return springDataMessageRepository.countByChatRoomId(chatRoomId);
    }
}