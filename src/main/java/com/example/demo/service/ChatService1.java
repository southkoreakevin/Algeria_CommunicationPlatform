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
import java.util.Objects;

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
                .map(existingRoom -> toChatRoomResponse(existingRoom, requester))
                .orElseGet(() -> {
                    ChatRoom room = chatRoomRepository.save(new ChatRoom(ChatRoomType.DIRECT));
                    chatRoomMemberRepository.save(new ChatRoomMember(room, requester));
                    chatRoomMemberRepository.save(new ChatRoomMember(room, target));
                    log.info("[CHAT] 채팅방 생성 - roomId: {}, {} <-> {}", room.getId(), requesterEmail, targetEmail);
                    return toChatRoomResponse(room, requester);
                });
    }

    @Override
    @Transactional
    public ChatRoomResponse createGroupRoom(String creatorEmail, String name, List<String> memberEmails) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("그룹 채팅방 이름을 입력해주세요.");
        }
        User creator = getUser(creatorEmail);
        ChatRoom room = chatRoomRepository.save(new ChatRoom(ChatRoomType.GROUP, name));
        chatRoomMemberRepository.save(new ChatRoomMember(room, creator));

        if (memberEmails != null) {
            memberEmails.stream()
                    .filter(email -> !Objects.equals(email, creatorEmail))
                    .distinct()
                    .forEach(email -> {
                        User member = getUser(email);
                        chatRoomMemberRepository.save(new ChatRoomMember(room, member));
                    });
        }

        log.info("[CHAT] 그룹 채팅방 생성 - roomId: {}, name: {}, creator: {}", room.getId(), name, creatorEmail);
        return toChatRoomResponse(room, creator);
    }

    @Override
    @Transactional
    public ChatRoomResponse addMember(Long roomId, String requesterEmail, String targetEmail) {
        User requester = getUser(requesterEmail);
        ChatRoom room = getRoom(roomId);

        if (room.getType() != ChatRoomType.GROUP) {
            throw new IllegalArgumentException("그룹 채팅방에만 멤버를 추가할 수 있습니다.");
        }
        if (!chatRoomMemberRepository.existsByChatRoomAndUser(room, requester)) {
            throw new IllegalArgumentException("채팅방에 접근 권한이 없습니다.");
        }

        User target = getUser(targetEmail);
        if (chatRoomMemberRepository.existsByChatRoomAndUser(room, target)) {
            throw new IllegalArgumentException("이미 채팅방에 참여 중인 유저입니다.");
        }

        chatRoomMemberRepository.save(new ChatRoomMember(room, target));
        log.info("[CHAT] 그룹 멤버 추가 - roomId: {}, target: {}", roomId, targetEmail);
        return toChatRoomResponse(room, requester);
    }

    @Override
    @Transactional
    public void leaveRoom(Long roomId, String email) {
        User user = getUser(email);
        ChatRoom room = getRoom(roomId);

        if (!chatRoomMemberRepository.existsByChatRoomAndUser(room, user)) {
            throw new IllegalArgumentException("채팅방에 참여하지 않은 유저입니다.");
        }

        chatRoomMemberRepository.deleteByChatRoomAndUser(room, user);
        log.info("[CHAT] 채팅방 나가기 - roomId: {}, email: {}", roomId, email);
    }

    @Override
    @Transactional
    public Long markAsRead(Long roomId, String email) {
        User user = getUser(email);
        ChatRoom room = getRoom(roomId);

        ChatRoomMember member = chatRoomMemberRepository.findByChatRoomAndUser(room, user)
                .orElseThrow(() -> new IllegalArgumentException("채팅방에 접근 권한이 없습니다."));

        // 방의 마지막 메시지 ID 조회 (메시지가 없으면 null 유지)
        List<Message> latest = messageRepository.findByChatRoomId(roomId, PageRequest.of(0, 1));
        if (!latest.isEmpty()) {
            member.markAsRead(latest.get(0).getId());
        }

        log.info("[CHAT] 읽음 처리 - roomId: {}, email: {}, lastReadMessageId: {}", roomId, email, member.getLastReadMessageId());
        return member.getLastReadMessageId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRooms(String email) {
        User user = getUser(email);
        return chatRoomMemberRepository.findByUser(user).stream()
                .map(member -> toChatRoomResponse(member.getChatRoom(), user))
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

        List<Message> messages = messageRepository.findByChatRoomId(roomId, PageRequest.of(page, 30));
        // DB는 DESC(최신순)로 페이징, 응답은 채팅 UI에 맞게 ASC(오래된→최신)로 반환
        return messages.reversed().stream()
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

        // 발신자는 자신이 보낸 메시지를 자동으로 읽음 처리
        chatRoomMemberRepository.findByChatRoomAndUser(room, sender)
                .ifPresent(m -> m.markAsRead(message.getId()));

        log.info("[CHAT] 메시지 저장 - roomId: {}, sender: {}", roomId, senderEmail);
        return new MessageResponse(message.getId(), senderEmail, content, message.getSentAt().toString());
    }

    private ChatRoomResponse toChatRoomResponse(ChatRoom room, User requestUser) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoom(room);
        List<String> memberEmails = members.stream()
                .map(m -> m.getUser().getEmail())
                .toList();

        long unreadCount = members.stream()
                .filter(m -> m.getUser().equals(requestUser))
                .findFirst()
                .map(m -> m.getLastReadMessageId() == null
                        ? messageRepository.countByChatRoomId(room.getId())
                        : messageRepository.countByChatRoomIdAndIdGreaterThan(room.getId(), m.getLastReadMessageId()))
                .orElse(0L);

        return new ChatRoomResponse(room.getId(), room.getType(), room.getName(), memberEmails, unreadCount);
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