package com.example.demo;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Slf4j
@RequiredArgsConstructor
public class TestDataInit {
    private final UserRepository userRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initData(){
        log.info("test data init");
        userRepository.save(new User("1234@gmail.com", "1234"));
        userRepository.save(new User("1222@gmail.com", "2222"));
    }
}
