package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.web.dto.UserJoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void join(UserJoinRequest request){
        if(userRepository.existByEmail(request.email())){
            throw new IllegalStateException("이미 존제하는 이메일 입니다.");
        }

        User user = new User(request.email(), request.password());
        userRepository.save(user);
    }
}
