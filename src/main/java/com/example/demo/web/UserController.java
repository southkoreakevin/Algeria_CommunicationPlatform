package com.example.demo.web;

import com.example.demo.repository.memory.ItemSearchDto;
import com.example.demo.service.UserService;
import com.example.demo.web.dto.LoginResponse;
import com.example.demo.web.dto.UserJoinRequest;
import com.example.demo.web.dto.UserLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public String join(@RequestBody UserJoinRequest request) {
        userService.join(request);
        return "Sign Up success";
    }

    @PostMapping("/idCheck")
    public boolean idCheck(@RequestBody ItemSearchDto dto){
        return userService.checkDuplicateEmail(dto.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserLoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

}
