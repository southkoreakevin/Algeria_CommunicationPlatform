package com.example.demo.web;

import com.example.demo.service.ChatService;
import com.example.demo.web.dto.ChatRoomResponse;
import com.example.demo.web.dto.CreateChatRoomRequest;
import com.example.demo.web.dto.MessageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(@RequestBody CreateChatRoomRequest request,
                                                       HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        log.info("[CHAT] 채팅방 생성 요청 - requester: {}, target: {}", email, request.targetEmail());
        return ResponseEntity.ok(chatService.createDirectRoom(email, request.targetEmail()));
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getRooms(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        return ResponseEntity.ok(chatService.getChatRooms(email));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable Long roomId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        return ResponseEntity.ok(chatService.getMessages(roomId, email, page));
    }
}