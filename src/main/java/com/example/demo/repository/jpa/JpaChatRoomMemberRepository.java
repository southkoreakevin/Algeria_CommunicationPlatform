package com.example.demo.repository.jpa;

import com.example.demo.domain.ChatRoom;
import com.example.demo.domain.ChatRoomMember;
import com.example.demo.domain.ChatRoomType;
import com.example.demo.domain.User;
import com.example.demo.repository.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaChatRoomMemberRepository implements ChatRoomMemberRepository {

    private final SpringDataChatRoomMemberRepository springDataChatRoomMemberRepository;

    @Override
    public void save(ChatRoomMember member) {
        springDataChatRoomMemberRepository.save(member);
    }

    @Override
    public List<ChatRoomMember> findByUser(User user) {
        return springDataChatRoomMemberRepository.findByUser(user);
    }

    @Override
    public List<ChatRoomMember> findByChatRoom(ChatRoom chatRoom) {
        return springDataChatRoomMemberRepository.findByChatRoom(chatRoom);
    }

    @Override
    public Optional<ChatRoom> findDirectRoomBetween(User user1, User user2) {
        return springDataChatRoomMemberRepository.findDirectRoomBetween(user1, user2, ChatRoomType.DIRECT);
    }

    @Override
    public boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user) {
        return springDataChatRoomMemberRepository.existsByChatRoomAndUser(chatRoom, user);
    }
}