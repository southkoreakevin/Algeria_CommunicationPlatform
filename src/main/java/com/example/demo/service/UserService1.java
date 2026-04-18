package com.example.demo.service;

import com.example.demo.config.JwtConfig;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.web.dto.LoginResponse;
import com.example.demo.web.dto.UserJoinRequest;
import com.example.demo.web.dto.UserLoginRequest;
import com.example.demo.web.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService1 implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    @Override
    public void join(UserJoinRequest request){
        if(userRepository.existByEmail(request.email())){
            throw new IllegalStateException("이미 존제하는 이메일 입니다.");
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    @Override
    public LoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getUserId())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));
        return new LoginResponse(jwtConfig.generateToken(user.getEmail()));
    }

    @Override
    public boolean checkDuplicateEmail(String email) {
        return userRepository.existByEmail(email);
    }

    @Override
    public UserResponse getMyInfo(String userId) {
        return null;
    }
}
