package com.example.demo.web.dto;

public record AlarmResponse(Long roomId, String senderEmail, String preview, String sentAt) {}