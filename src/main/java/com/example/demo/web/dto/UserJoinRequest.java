package com.example.demo.web.dto;

public record UserJoinRequest(
    String email,
    String password
) { }
