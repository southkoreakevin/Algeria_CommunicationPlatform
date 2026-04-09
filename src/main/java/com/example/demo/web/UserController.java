package com.example.demo.web;

import com.example.demo.service.UserService;
import com.example.demo.web.dto.UserJoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/join")
    public String join(@RequestBody UserJoinRequest request){
        userService.join(request);
        return "회원가입 성공";
    }
}
