package com.example.demo.web;

import com.example.demo.service.FriendService;
import com.example.demo.web.dto.FriendRequestDto;
import com.example.demo.web.dto.FriendRequestResponse;
import com.example.demo.web.dto.FriendResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(@RequestBody FriendRequestDto dto, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        log.info("[FRIEND] 친구 요청 - from: {} to: {}", email, dto.receiverEmail());
        try {
            friendService.sendRequest(email, dto.receiverEmail());
            return ResponseEntity.ok("친구 요청을 보냈습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<String> accept(@PathVariable Long friendshipId, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        try {
            friendService.accept(email, friendshipId);
            return ResponseEntity.ok("친구 요청을 수락했습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reject/{friendshipId}")
    public ResponseEntity<String> reject(@PathVariable Long friendshipId, HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        try {
            friendService.reject(email, friendshipId);
            return ResponseEntity.ok("친구 요청을 거절했습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<FriendResponse>> getFriends(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        return ResponseEntity.ok(friendService.getFriends(email));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendRequestResponse>> getPendingRequests(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        return ResponseEntity.ok(friendService.getPendingRequests(email));
    }
}