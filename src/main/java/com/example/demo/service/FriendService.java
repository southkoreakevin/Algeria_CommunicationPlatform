package com.example.demo.service;

import com.example.demo.web.dto.FriendRequestResponse;
import com.example.demo.web.dto.FriendResponse;

import java.util.List;

public interface FriendService {
    void sendRequest(String requesterEmail, String receiverEmail);
    void accept(String receiverEmail, Long friendshipId);
    void reject(String receiverEmail, Long friendshipId);
    List<FriendResponse> getFriends(String email);
    List<FriendRequestResponse> getPendingRequests(String email);
}