package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.ChatRoomMemberRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.web.dto.ChatRoomResponse;
import com.example.demo.web.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService1 implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatRoomResponse createDirectRoom(String requesterEmail, String targetEmail) {
        User requester = getUser(requesterEmail);
        User target = getUser(targetEmail);

        return chatRoomMemberRepository.findDirectRoomBetween(requester, target)
                .map(existingRoom -> toChatRoomResponse(existingRoom))
                .orElseGet(() -> {
                    ChatRoom room = chatRoomRepository.save(new ChatRoom(ChatRoomType.DIRECT));
                    chatRoomMemberRepository.save(new ChatRoomMember(room, requester));
                    chatRoomMemberRepository.save(new ChatRoomMember(room, target));
                    log.info("[CHAT] 채팅방 생성 - roomId: {}, {} <-> {}", room.getId(), requesterEmail, targetEmail);
                    return toChatRoomResponse(room);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRooms(String email) {
        User user = getUser(email);
        return chatRoomMemberRepository.findByUser(user).stream()
                .map(member -> toChatRoomResponse(member.getChatRoom()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long roomId, String email, int page) {
        User user = getUser(email);
        ChatRoom room = getRoom(roomId);

        if (!chatRoomMemberRepository.existsByChatRoomAndUser(room, user)) {
            throw new IllegalArgumentException("채팅방에 접근 권한이 없습니다.");
        }

        return messageRepository.findByChatRoomId(roomId, PageRequest.of(page, 30)).stream()
                .map(m -> new MessageResponse(m.getId(), m.getSender().getEmail(), m.getContent(), m.getSentAt().toString()))
                .toList();
    }

    @Override
    @Transactional
    public MessageResponse saveMessage(Long roomId, String senderEmail, String content) {
        User sender = getUser(senderEmail);
        ChatRoom room = getRoom(roomId);

        if (!chatRoomMemberRepository.existsByChatRoomAndUser(room, sender)) {
            throw new IllegalArgumentException("채팅방에 접근 권한이 없습니다.");
        }

        Message message = new Message(room, sender, content);
        messageRepository.save(message);
        log.info("[CHAT] 메시지 저장 - roomId: {}, sender: {}", roomId, senderEmail);
        return new MessageResponse(message.getId(), senderEmail, content, message.getSentAt().toString());
    }

    private ChatRoomResponse toChatRoomResponse(ChatRoom room) {
        List<String> memberEmails = chatRoomMemberRepository.findByChatRoom(room).stream()
                .map(m -> m.getUser().getEmail())
                .toList();
        return new ChatRoomResponse(room.getId(), room.getType(), memberEmails);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다: " + email));
    }

    private ChatRoom getRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));
    }
}