package com.example.demo.service;

import com.example.demo.web.dto.ChatRoomResponse;
import com.example.demo.web.dto.MessageResponse;

import java.util.List;

public interface ChatService {
    ChatRoomResponse createDirectRoom(String requesterEmail, String targetEmail);
    ChatRoomResponse createGroupRoom(String creatorEmail, String name, List<String> memberEmails);
    ChatRoomResponse addMember(Long roomId, String requesterEmail, String targetEmail);
    void leaveRoom(Long roomId, String email);
    Long markAsRead(Long roomId, String email);
    List<String> getRoomMemberEmailsExcept(Long roomId, String excludeEmail);
    List<ChatRoomResponse> getChatRooms(String email);
    List<MessageResponse> getMessages(Long roomId, String email, int page);
    MessageResponse saveMessage(Long roomId, String senderEmail, String content);
}