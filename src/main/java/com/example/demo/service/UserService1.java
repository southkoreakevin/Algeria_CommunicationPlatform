package com.example.demo.service;

import com.example.demo.config.JwtConfig;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.web.dto.LoginResponse;
import com.example.demo.web.dto.UserJoinRequest;
import com.example.demo.web.dto.UserLoginRequest;
import com.example.demo.web.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService1 implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    @Override
    public void join(UserJoinRequest request){
        log.info("[JOIN] 이메일 중복 체크 - email: {}", request.email());
        if(userRepository.existByEmail(request.email())){
            log.warn("[JOIN] 이미 존재하는 이메일 - email: {}", request.email());
            throw new IllegalStateException("이미 존제하는 이메일 입니다.");
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);
        log.info("[JOIN] DB 저장 완료 - email: {}", request.email());
    }

    @Override
    public LoginResponse login(UserLoginRequest request) {
        log.info("[LOGIN] DB에서 유저 조회 - email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("[LOGIN] 존재하지 않는 이메일 - email: {}", request.getEmail());
                    return new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("[LOGIN] 비밀번호 불일치 - email: {}", request.getEmail());
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        log.info("[LOGIN] 비밀번호 일치, 토큰 발급 - email: {}", request.getEmail());
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
