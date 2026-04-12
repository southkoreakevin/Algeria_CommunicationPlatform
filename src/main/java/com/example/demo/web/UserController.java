package com.example.demo.web;

import com.example.demo.repository.memory.ItemSearchDto;
import com.example.demo.service.UserService1;
import com.example.demo.web.dto.UserJoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService1 userService1;

    @PostMapping("/join")
    public String join(@RequestBody UserJoinRequest request) {

        return "Sing Up success";
    }

    @PostMapping("/idCheck")
    public boolean idCheck(@RequestBody ItemSearchDto dto){
        String email = dto.getEmail();

    }

}
