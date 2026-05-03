package com.example.demo.web;

import com.example.demo.service.ChatService;
import com.example.demo.web.dto.AlarmResponse;
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

    private static final int PREVIEW_MAX_LENGTH = 30;

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId,
                            MessageDto messageDto,
                            Principal principal) {
        String senderEmail = principal.getName();
        log.info("[CHAT] 메시지 수신 - roomId: {}, sender: {}", roomId, senderEmail);

        MessageResponse response = chatService.saveMessage(roomId, senderEmail, messageDto.content());

        // 채팅방 구독자 전체에게 메시지 전송
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, response);

        // 발신자를 제외한 멤버 각각에게 개인 알람 전송
        AlarmResponse alarm = new AlarmResponse(roomId, senderEmail, preview(messageDto.content()), response.sentAt());
        chatService.getRoomMemberEmailsExcept(roomId, senderEmail)
                .forEach(email -> {
                    log.debug("[ALARM] 알람 전송 - to: {}, roomId: {}", email, roomId);
                    messagingTemplate.convertAndSend("/topic/alarm/" + email, alarm);
                });
    }

    private String preview(String content) {
        if (content.length() <= PREVIEW_MAX_LENGTH) return content;
        return content.substring(0, PREVIEW_MAX_LENGTH) + "...";
    }
}