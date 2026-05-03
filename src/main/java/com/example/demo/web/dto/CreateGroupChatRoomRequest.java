package com.example.demo.web.dto;

import java.util.List;

public record CreateGroupChatRoomRequest(String name, List<String> memberEmails) {}