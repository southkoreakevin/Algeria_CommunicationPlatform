package com.example.demo.web;

import com.example.demo.service.ChatService;
import com.example.demo.web.dto.AlarmResponse;
import com.example.demo.web.dto.MessageDto;
import com.example.demo.web.dto.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock ChatService chatService;
    @Mock SimpMessagingTemplate messagingTemplate;
    @Mock Principal principal;

    @InjectMocks ChatController chatController;

    private MessageResponse savedMessage;

    @BeforeEach
    void setUp() {
        given(principal.getName()).willReturn("a@test.com");
        savedMessage = new MessageResponse(1L, "a@test.com", "안녕하세요", "2026-05-04T12:00:00");
        given(chatService.saveMessage(anyLong(), anyString(), anyString())).willReturn(savedMessage);
    }

    // =========================================================================
    // sendMessage - 메시지 브로드캐스트
    // =========================================================================

    @Nested
    @DisplayName("sendMessage - 메시지 브로드캐스트")
    class SendMessageBroadcast {

        @Test
        @DisplayName("채팅방 토픽에 메시지 브로드캐스트")
        void 채팅방_토픽_브로드캐스트() {
            given(chatService.getRoomMemberEmailsExcept(10L, "a@test.com")).willReturn(List.of("b@test.com"));

            chatController.sendMessage(10L, new MessageDto("안녕하세요"), principal);

            then(messagingTemplate).should().convertAndSend("/topic/chat/10", savedMessage);
        }

        @Test
        @DisplayName("발신자를 제외한 멤버에게 알람 전송")
        void 알람_전송() {
            given(chatService.getRoomMemberEmailsExcept(10L, "a@test.com"))
                    .willReturn(List.of("b@test.com", "c@test.com"));

            chatController.sendMessage(10L, new MessageDto("안녕하세요"), principal);

            then(messagingTemplate).should().convertAndSend(eq("/topic/alarm/b@test.com"), any(AlarmResponse.class));
            then(messagingTemplate).should().convertAndSend(eq("/topic/alarm/c@test.com"), any(AlarmResponse.class));
        }

        @Test
        @DisplayName("알람에 roomId, senderEmail, preview, sentAt 포함")
        void 알람_내용_확인() {
            given(chatService.getRoomMemberEmailsExcept(10L, "a@test.com")).willReturn(List.of("b@test.com"));

            chatController.sendMessage(10L, new MessageDto("안녕하세요"), principal);

            ArgumentCaptor<AlarmResponse> captor = ArgumentCaptor.forClass(AlarmResponse.class);
            then(messagingTemplate).should().convertAndSend(eq("/topic/alarm/b@test.com"), captor.capture());

            AlarmResponse alarm = captor.getValue();
            assertThat(alarm.roomId()).isEqualTo(10L);
            assertThat(alarm.senderEmail()).isEqualTo("a@test.com");
            assertThat(alarm.preview()).isEqualTo("안녕하세요");
            assertThat(alarm.sentAt()).isEqualTo("2026-05-04T12:00:00");
        }
    }

    // =========================================================================
    // sendMessage - 발신자 알람 제외
    // =========================================================================

    @Nested
    @DisplayName("sendMessage - 발신자 알람 제외")
    class SenderAlarmExclusion {

        @Test
        @DisplayName("발신자 본인에게는 알람을 보내지 않음")
        void 발신자_알람_미전송() {
            given(chatService.getRoomMemberEmailsExcept(10L, "a@test.com")).willReturn(List.of());

            chatController.sendMessage(10L, new MessageDto("안녕하세요"), principal);

            then(messagingTemplate).should(never())
                    .convertAndSend(eq("/topic/alarm/a@test.com"), any(AlarmResponse.class));
        }

        @Test
        @DisplayName("채팅방 멤버가 발신자뿐이면 알람 없음")
        void 발신자만_있으면_알람없음() {
            given(chatService.getRoomMemberEmailsExcept(10L, "a@test.com")).willReturn(List.of());

            chatController.sendMessage(10L, new MessageDto("혼자"), principal);

            // 채팅방 메시지 브로드캐스트는 여전히 발생
            then(messagingTemplate).should().convertAndSend("/topic/chat/10", savedMessage);
            // 알람은 없음
            then(messagingTemplate).should(never()).convertAndSend(contains("/topic/alarm/"), any(AlarmResponse.class));
        }
    }

    // =========================================================================
    // sendMessage - preview 자르기
    // =========================================================================

    @Nested
    @DisplayName("sendMessage - preview 길이 제한")
    class PreviewTruncation {

        @Test
        @DisplayName("30자 이하 메시지는 preview 그대로")
        void 짧은메시지_그대로() {
            given(chatService.getRoomMemberEmailsExcept(10L, "a@test.com")).willReturn(List.of("b@test.com"));
            String shortMsg = "짧은 메시지";
            given(chatService.saveMessage(10L, "a@test.com", shortMsg))
                    .willReturn(new MessageResponse(2L, "a@test.com", shortMsg, "2026-05-04T12:00:00"));

            chatController.sendMessage(10L, new MessageDto(shortMsg), principal);

            ArgumentCaptor<AlarmResponse> captor = ArgumentCaptor.forClass(AlarmResponse.class);
            then(messagingTemplate).should().convertAndSend(eq("/topic/alarm/b@test.com"), captor.capture());
            assertThat(captor.getValue().preview()).isEqualTo(shortMsg);
        }

        @Test
        @DisplayName("30자 초과 메시지는 preview 30자+... 로 자름")
        void 긴메시지_자르기() {
            given(chatService.getRoomMemberEmailsExcept(10L, "a@test.com")).willReturn(List.of("b@test.com"));
            String longMsg = "가".repeat(50); // 50자
            given(chatService.saveMessage(10L, "a@test.com", longMsg))
                    .willReturn(new MessageResponse(3L, "a@test.com", longMsg, "2026-05-04T12:00:00"));

            chatController.sendMessage(10L, new MessageDto(longMsg), principal);

            ArgumentCaptor<AlarmResponse> captor = ArgumentCaptor.forClass(AlarmResponse.class);
            then(messagingTemplate).should().convertAndSend(eq("/topic/alarm/b@test.com"), captor.capture());
            assertThat(captor.getValue().preview()).isEqualTo("가".repeat(30) + "...");
        }

        @Test
        @DisplayName("정확히 30자면 자르지 않음")
        void 딱30자_자르기없음() {
            given(chatService.getRoomMemberEmailsExcept(10L, "a@test.com")).willReturn(List.of("b@test.com"));
            String exactMsg = "나".repeat(30); // 정확히 30자
            given(chatService.saveMessage(10L, "a@test.com", exactMsg))
                    .willReturn(new MessageResponse(4L, "a@test.com", exactMsg, "2026-05-04T12:00:00"));

            chatController.sendMessage(10L, new MessageDto(exactMsg), principal);

            ArgumentCaptor<AlarmResponse> captor = ArgumentCaptor.forClass(AlarmResponse.class);
            then(messagingTemplate).should().convertAndSend(eq("/topic/alarm/b@test.com"), captor.capture());
            assertThat(captor.getValue().preview()).isEqualTo(exactMsg);
            assertThat(captor.getValue().preview()).doesNotEndWith("...");
        }
    }
}