package com.example.demo.service;

import com.example.demo.repository.UserRepository;
import com.example.demo.web.dto.UserJoinRequest;
import com.example.demo.web.dto.UserLoginRequest;
import com.example.demo.web.dto.UserResponse;

public interface UserService {

    void join(UserJoinRequest request);

    boolean login(UserLoginRequest reqeust);

    boolean checkDuplicateEmail(String email);

    UserResponse getMyInfo(String email);

}
