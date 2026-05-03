package com.example.demo.web.dto;

public record ReadReceiptEvent(String email, Long lastReadMessageId) {}