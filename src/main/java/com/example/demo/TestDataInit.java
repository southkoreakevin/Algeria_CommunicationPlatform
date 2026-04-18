package com.example.demo;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
@Profile("dev")
public class TestDataInit {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initData(){
        log.info("test data init");
        String encodedPassword1 = passwordEncoder.encode("1234");
        userRepository.save(new User("1234@gmail.com", encodedPassword1));
        String encodedPassword2 = passwordEncoder.encode("2222");
        userRepository.save(new User("1222@gmail.com", encodedPassword2));
    }
}
