package com.example.demo.web;

import com.example.demo.service.ChatService;
import com.example.demo.web.dto.MessageDto;
import com.example.demo.web.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId,
                            MessageDto messageDto,
                            Principal principal) {
        String email = principal.getName();
        log.info("[CHAT] 메시지 수신 - roomId: {}, sender: {}", roomId, email);
        MessageResponse response = chatService.saveMessage(roomId, email, messageDto.content());
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);
    }
}