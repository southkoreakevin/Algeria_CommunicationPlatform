package com.example.demo.service;

import com.example.demo.web.dto.ChatRoomResponse;
import com.example.demo.web.dto.MessageResponse;

import java.util.List;

public interface ChatService {
    ChatRoomResponse createDirectRoom(String requesterEmail, String targetEmail);
    List<ChatRoomResponse> getChatRooms(String email);
    List<MessageResponse> getMessages(Long roomId, String email, int page);
    MessageResponse saveMessage(Long roomId, String senderEmail, String content);
}