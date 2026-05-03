package com.example.demo.web;

import com.example.demo.repository.memory.ItemSearchDto;
import com.example.demo.service.UserService;
import com.example.demo.web.dto.LoginResponse;
import com.example.demo.web.dto.UserJoinRequest;
import com.example.demo.web.dto.UserLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.demo.web.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody UserJoinRequest request) {
        log.info("[JOIN] 회원가입 요청 - email: {}", request.email());
        try {
            userService.join(request);
            log.info("[JOIN] 회원가입 성공 - email: {}", request.email());
            return ResponseEntity.ok("Sign Up success");
        } catch (IllegalStateException e) {
            log.warn("[JOIN] 회원가입 실패 - {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getMyInfo(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        return ResponseEntity.ok(userService.getMyInfo(email));
    }

    @PostMapping("/idCheck")
    public boolean idCheck(@RequestBody ItemSearchDto dto){
        log.info("[ID_CHECK] 중복확인 요청 - email: {}", dto.getEmail());
        return userService.checkDuplicateEmail(dto.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserLoginRequest request) {
        log.info("[LOGIN] 로그인 요청 - email: {}", request.getEmail());
        try {
            LoginResponse response = userService.login(request);
            log.info("[LOGIN] 로그인 성공 - email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("[LOGIN] 로그인 실패 - {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
