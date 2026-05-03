package com.example.demo.web.dto;

import com.example.demo.domain.ChatRoomType;

import java.util.List;

public record ChatRoomResponse(Long id, ChatRoomType type, String name, List<String> memberEmails) {}