package com.example.demo.repository.jpa;

import com.example.demo.domain.ChatRoom;
import com.example.demo.domain.ChatRoomMember;
import com.example.demo.domain.ChatRoomType;
import com.example.demo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpringDataChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    List<ChatRoomMember> findByUser(User user);
    List<ChatRoomMember> findByChatRoom(ChatRoom chatRoom);
    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);
    void deleteByChatRoomAndUser(ChatRoom chatRoom, User user);

    @Query("SELECT m.chatRoom FROM ChatRoomMember m " +
           "WHERE m.chatRoom.type = :type " +
           "AND m.user = :user1 " +
           "AND EXISTS (SELECT m2 FROM ChatRoomMember m2 WHERE m2.chatRoom = m.chatRoom AND m2.user = :user2)")
    Optional<ChatRoom> findDirectRoomBetween(@Param("user1") User user1,
                                             @Param("user2") User user2,
                                             @Param("type") ChatRoomType type);
}