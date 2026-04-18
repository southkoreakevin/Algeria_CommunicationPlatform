package com.example.demo.domain;

import lombok.Getter;

@Getter
public class User {

    private Long id;
    private String email;
    private String password;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public void assignId(Long id) {
        this.id = id;
    }

}