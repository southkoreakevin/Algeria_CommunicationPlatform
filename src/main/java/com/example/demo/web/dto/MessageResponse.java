package com.example.demo.web.dto;

public record MessageResponse(Long id, String senderEmail, String content, String sentAt) {}