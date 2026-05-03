package com.example.demo.web;

import com.example.demo.service.ChatService;
import com.example.demo.web.dto.AddMemberRequest;
import com.example.demo.web.dto.ChatRoomResponse;
import com.example.demo.web.dto.CreateChatRoomRequest;
import com.example.demo.web.dto.CreateGroupChatRoomRequest;
import com.example.demo.web.dto.MessageResponse;
import com.example.demo.web.dto.ReadReceiptEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createRoom(@RequestBody CreateChatRoomRequest request,
                                                       HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        log.info("[CHAT] 1:1 채팅방 생성 요청 - requester: {}, target: {}", email, request.targetEmail());
        return ResponseEntity.ok(chatService.createDirectRoom(email, request.targetEmail()));
    }

    @PostMapping("/rooms/group")
    public ResponseEntity<ChatRoomResponse> createGroupRoom(@RequestBody CreateGroupChatRoomRequest request,
                                                            HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        log.info("[CHAT] 그룹 채팅방 생성 요청 - creator: {}, name: {}", email, request.name());
        return ResponseEntity.ok(chatService.createGroupRoom(email, request.name(), request.memberEmails()));
    }

    @PostMapping("/rooms/{roomId}/members")
    public ResponseEntity<ChatRoomResponse> addMember(@PathVariable Long roomId,
                                                      @RequestBody AddMemberRequest request,
                                                      HttpServletRequest httpRequest) {
        String email = (String) httpRequest.getAttribute("email");
        log.info("[CHAT] 그룹 멤버 추가 요청 - roomId: {}, requester: {}, target: {}", roomId, email, request.targetEmail());
        return ResponseEntity.ok(chatService.addMember(roomId, email, request.targetEmail()));
    }

    @DeleteMapping("/rooms/{roomId}/members/me")
    public ResponseEntity<Void> leaveRoom(@PathVariable Long roomId, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        log.info("[CHAT] 채팅방 나가기 요청 - roomId: {}, email: {}", roomId, email);
        chatService.leaveRoom(roomId, email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{roomId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long roomId, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        Long lastReadMessageId = chatService.markAsRead(roomId, email);
        if (lastReadMessageId != null) {
            messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/read",
                    new ReadReceiptEvent(email, lastReadMessageId));
        }
        return ResponseEntity.noContent().build();
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