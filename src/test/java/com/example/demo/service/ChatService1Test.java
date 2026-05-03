package com.example.demo.service;

import com.example.demo.domain.*;
import com.example.demo.repository.*;
import com.example.demo.web.dto.ChatRoomResponse;
import com.example.demo.web.dto.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ChatService1Test {

    @Mock ChatRoomRepository chatRoomRepository;
    @Mock ChatRoomMemberRepository chatRoomMemberRepository;
    @Mock MessageRepository messageRepository;
    @Mock UserRepository userRepository;

    @InjectMocks ChatService1 chatService;

    private User userA, userB, userC;
    private ChatRoom directRoom, groupRoom;

    @BeforeEach
    void setUp() {
        userA = user(1L, "a@test.com");
        userB = user(2L, "b@test.com");
        userC = user(3L, "c@test.com");
        directRoom = chatRoom(10L, ChatRoomType.DIRECT, null);
        groupRoom  = chatRoom(20L, ChatRoomType.GROUP, "팀 채팅");
    }

    // =========================================================================
    // createDirectRoom
    // =========================================================================

    @Nested
    @DisplayName("createDirectRoom")
    class CreateDirectRoom {

        @Test
        @DisplayName("이미 방이 있으면 기존 방 반환 (새 방 생성 X)")
        void 기존방_반환() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(userRepository.findByEmail("b@test.com")).willReturn(Optional.of(userB));
            given(chatRoomMemberRepository.findDirectRoomBetween(userA, userB)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.findByChatRoom(directRoom))
                    .willReturn(List.of(new ChatRoomMember(directRoom, userA), new ChatRoomMember(directRoom, userB)));
            given(messageRepository.countByChatRoomId(10L)).willReturn(0L);

            ChatRoomResponse response = chatService.createDirectRoom("a@test.com", "b@test.com");

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.type()).isEqualTo(ChatRoomType.DIRECT);
            then(chatRoomRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("새 방 생성 시 두 멤버 모두 저장")
        void 새방_생성() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(userRepository.findByEmail("b@test.com")).willReturn(Optional.of(userB));
            given(chatRoomMemberRepository.findDirectRoomBetween(userA, userB)).willReturn(Optional.empty());
            given(chatRoomRepository.save(any())).willReturn(directRoom);
            given(chatRoomMemberRepository.findByChatRoom(directRoom))
                    .willReturn(List.of(new ChatRoomMember(directRoom, userA), new ChatRoomMember(directRoom, userB)));
            given(messageRepository.countByChatRoomId(10L)).willReturn(0L);

            ChatRoomResponse response = chatService.createDirectRoom("a@test.com", "b@test.com");

            assertThat(response.id()).isEqualTo(10L);
            assertThat(response.memberEmails()).hasSize(2);
            then(chatRoomMemberRepository).should(times(2)).save(any(ChatRoomMember.class));
        }

        @Test
        @DisplayName("존재하지 않는 유저면 예외")
        void 없는_유저_예외() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> chatService.createDirectRoom("a@test.com", "b@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 유저");
        }
    }

    // =========================================================================
    // createGroupRoom
    // =========================================================================

    @Nested
    @DisplayName("createGroupRoom")
    class CreateGroupRoom {

        @Test
        @DisplayName("정상 생성: 생성자 + 멤버 모두 추가")
        void 정상생성() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(userRepository.findByEmail("b@test.com")).willReturn(Optional.of(userB));
            given(userRepository.findByEmail("c@test.com")).willReturn(Optional.of(userC));
            given(chatRoomRepository.save(any())).willReturn(groupRoom);
            given(chatRoomMemberRepository.findByChatRoom(groupRoom))
                    .willReturn(List.of(new ChatRoomMember(groupRoom, userA),
                                        new ChatRoomMember(groupRoom, userB),
                                        new ChatRoomMember(groupRoom, userC)));
            given(messageRepository.countByChatRoomId(20L)).willReturn(0L);

            ChatRoomResponse response = chatService.createGroupRoom(
                    "a@test.com", "팀 채팅", List.of("b@test.com", "c@test.com"));

            assertThat(response.type()).isEqualTo(ChatRoomType.GROUP);
            assertThat(response.name()).isEqualTo("팀 채팅");
            // creator + b + c = 3번 save
            then(chatRoomMemberRepository).should(times(3)).save(any(ChatRoomMember.class));
        }

        @Test
        @DisplayName("memberEmails에 자기 자신이 포함돼도 중복 추가 안 됨")
        void 자기자신_중복제거() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(userRepository.findByEmail("b@test.com")).willReturn(Optional.of(userB));
            given(chatRoomRepository.save(any())).willReturn(groupRoom);
            given(chatRoomMemberRepository.findByChatRoom(groupRoom))
                    .willReturn(List.of(new ChatRoomMember(groupRoom, userA),
                                        new ChatRoomMember(groupRoom, userB)));
            given(messageRepository.countByChatRoomId(20L)).willReturn(0L);

            chatService.createGroupRoom("a@test.com", "팀 채팅", List.of("a@test.com", "b@test.com"));

            // creator(a) + b = 2번만 save (a 중복 제거)
            then(chatRoomMemberRepository).should(times(2)).save(any(ChatRoomMember.class));
        }

        @Test
        @DisplayName("이름이 빈 문자열이면 예외")
        void 이름없으면_예외() {
            assertThatThrownBy(() -> chatService.createGroupRoom("a@test.com", "", List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이름");
        }

        @Test
        @DisplayName("이름이 공백만 있어도 예외")
        void 이름공백_예외() {
            assertThatThrownBy(() -> chatService.createGroupRoom("a@test.com", "   ", List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // =========================================================================
    // markAsRead
    // =========================================================================

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("방의 최신 메시지 ID로 lastReadMessageId 갱신")
        void 최신메시지로_갱신() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));

            ChatRoomMember memberA = new ChatRoomMember(directRoom, userA);
            given(chatRoomMemberRepository.findByChatRoomAndUser(directRoom, userA))
                    .willReturn(Optional.of(memberA));

            Message latest = message(99L, directRoom, userB, "hi");
            given(messageRepository.findByChatRoomId(10L, PageRequest.of(0, 1)))
                    .willReturn(List.of(latest));

            Long result = chatService.markAsRead(10L, "a@test.com");

            assertThat(result).isEqualTo(99L);
            assertThat(memberA.getLastReadMessageId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("메시지가 없으면 lastReadMessageId는 null 유지")
        void 메시지없으면_null유지() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));

            ChatRoomMember memberA = new ChatRoomMember(directRoom, userA);
            given(chatRoomMemberRepository.findByChatRoomAndUser(directRoom, userA))
                    .willReturn(Optional.of(memberA));
            given(messageRepository.findByChatRoomId(10L, PageRequest.of(0, 1)))
                    .willReturn(List.of());

            Long result = chatService.markAsRead(10L, "a@test.com");

            assertThat(result).isNull();
            assertThat(memberA.getLastReadMessageId()).isNull();
        }

        @Test
        @DisplayName("lastReadMessageId는 감소하지 않음 (더 오래된 메시지로 되돌아가지 않음)")
        void lastReadMessageId_감소안함() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));

            ChatRoomMember memberA = new ChatRoomMember(directRoom, userA);
            ReflectionTestUtils.setField(memberA, "lastReadMessageId", 50L);
            given(chatRoomMemberRepository.findByChatRoomAndUser(directRoom, userA))
                    .willReturn(Optional.of(memberA));

            // 현재 방의 "최신" 메시지가 ID=30 (오래된 것)인 경우
            Message oldMsg = message(30L, directRoom, userB, "old");
            given(messageRepository.findByChatRoomId(10L, PageRequest.of(0, 1)))
                    .willReturn(List.of(oldMsg));

            chatService.markAsRead(10L, "a@test.com");

            // 50 → 30으로 감소하면 안 됨
            assertThat(memberA.getLastReadMessageId()).isEqualTo(50L);
        }

        @Test
        @DisplayName("채팅방 멤버가 아니면 예외")
        void 권한없으면_예외() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.findByChatRoomAndUser(directRoom, userA))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> chatService.markAsRead(10L, "a@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("접근 권한");
        }
    }

    // =========================================================================
    // saveMessage (발신자 자동 읽음 처리)
    // =========================================================================

    @Nested
    @DisplayName("saveMessage")
    class SaveMessage {

        @Test
        @DisplayName("메시지 전송 시 발신자 lastReadMessageId 자동 갱신")
        void 발신자_자동읽음처리() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(directRoom, userA)).willReturn(true);

            // JPA persist 동작 시뮬레이션: save 호출 시 ID 부여
            doAnswer(inv -> {
                Message m = inv.getArgument(0);
                ReflectionTestUtils.setField(m, "id", 100L);
                return null;
            }).when(messageRepository).save(any(Message.class));

            ChatRoomMember memberA = new ChatRoomMember(directRoom, userA);
            given(chatRoomMemberRepository.findByChatRoomAndUser(directRoom, userA))
                    .willReturn(Optional.of(memberA));

            MessageResponse response = chatService.saveMessage(10L, "a@test.com", "안녕");

            assertThat(response.content()).isEqualTo("안녕");
            assertThat(response.senderEmail()).isEqualTo("a@test.com");
            // 발신자의 lastReadMessageId가 100으로 세팅됨
            assertThat(memberA.getLastReadMessageId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("채팅방 멤버가 아니면 메시지 전송 불가")
        void 권한없으면_예외() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(directRoom, userA)).willReturn(false);

            assertThatThrownBy(() -> chatService.saveMessage(10L, "a@test.com", "안녕"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("접근 권한");
        }
    }

    // =========================================================================
    // getChatRooms (unreadCount)
    // =========================================================================

    @Nested
    @DisplayName("getChatRooms - unreadCount")
    class GetChatRooms {

        @Test
        @DisplayName("한 번도 읽지 않았으면 (lastReadMessageId=null) 전체 메시지 수가 unreadCount")
        void 한번도_안읽으면_전체개수() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));

            ChatRoomMember memberA = new ChatRoomMember(directRoom, userA); // lastReadMessageId = null
            given(chatRoomMemberRepository.findByUser(userA)).willReturn(List.of(memberA));
            given(chatRoomMemberRepository.findByChatRoom(directRoom))
                    .willReturn(List.of(memberA, new ChatRoomMember(directRoom, userB)));
            given(messageRepository.countByChatRoomId(10L)).willReturn(5L);

            List<ChatRoomResponse> rooms = chatService.getChatRooms("a@test.com");

            assertThat(rooms).hasSize(1);
            assertThat(rooms.get(0).unreadCount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("lastReadMessageId 이후 메시지 수가 unreadCount")
        void lastReadMessageId_이후개수() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));

            ChatRoomMember memberA = new ChatRoomMember(directRoom, userA);
            ReflectionTestUtils.setField(memberA, "lastReadMessageId", 50L);
            given(chatRoomMemberRepository.findByUser(userA)).willReturn(List.of(memberA));
            given(chatRoomMemberRepository.findByChatRoom(directRoom))
                    .willReturn(List.of(memberA, new ChatRoomMember(directRoom, userB)));
            given(messageRepository.countByChatRoomIdAndIdGreaterThan(10L, 50L)).willReturn(3L);

            List<ChatRoomResponse> rooms = chatService.getChatRooms("a@test.com");

            assertThat(rooms.get(0).unreadCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("모두 읽었으면 unreadCount = 0")
        void 모두읽으면_0() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));

            ChatRoomMember memberA = new ChatRoomMember(directRoom, userA);
            ReflectionTestUtils.setField(memberA, "lastReadMessageId", 99L);
            given(chatRoomMemberRepository.findByUser(userA)).willReturn(List.of(memberA));
            given(chatRoomMemberRepository.findByChatRoom(directRoom))
                    .willReturn(List.of(memberA, new ChatRoomMember(directRoom, userB)));
            given(messageRepository.countByChatRoomIdAndIdGreaterThan(10L, 99L)).willReturn(0L);

            List<ChatRoomResponse> rooms = chatService.getChatRooms("a@test.com");

            assertThat(rooms.get(0).unreadCount()).isEqualTo(0L);
        }
    }

    // =========================================================================
    // getRoomMemberEmailsExcept (알람용 멤버 조회)
    // =========================================================================

    @Nested
    @DisplayName("getRoomMemberEmailsExcept")
    class GetRoomMemberEmailsExcept {

        @Test
        @DisplayName("발신자를 제외한 멤버 이메일 목록 반환")
        void 발신자_제외() {
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.findByChatRoom(directRoom))
                    .willReturn(List.of(new ChatRoomMember(directRoom, userA),
                                        new ChatRoomMember(directRoom, userB)));

            List<String> result = chatService.getRoomMemberEmailsExcept(10L, "a@test.com");

            assertThat(result).containsExactly("b@test.com");
            assertThat(result).doesNotContain("a@test.com");
        }

        @Test
        @DisplayName("그룹 방에서 발신자 제외 나머지 전원 반환")
        void 그룹방_발신자_제외() {
            given(chatRoomRepository.findById(20L)).willReturn(Optional.of(groupRoom));
            given(chatRoomMemberRepository.findByChatRoom(groupRoom))
                    .willReturn(List.of(new ChatRoomMember(groupRoom, userA),
                                        new ChatRoomMember(groupRoom, userB),
                                        new ChatRoomMember(groupRoom, userC)));

            List<String> result = chatService.getRoomMemberEmailsExcept(20L, "a@test.com");

            assertThat(result).containsExactlyInAnyOrder("b@test.com", "c@test.com");
        }

        @Test
        @DisplayName("1:1 방에서 자신만 있으면 빈 리스트")
        void 멤버_없으면_빈리스트() {
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.findByChatRoom(directRoom))
                    .willReturn(List.of(new ChatRoomMember(directRoom, userA)));

            List<String> result = chatService.getRoomMemberEmailsExcept(10L, "a@test.com");

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // getMessages (페이징 정렬)
    // =========================================================================

    @Nested
    @DisplayName("getMessages - 페이징 및 정렬")
    class GetMessages {

        @Test
        @DisplayName("응답은 오래된→최신 순(ASC) 정렬")
        void 응답_ASC정렬() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(directRoom, userA)).willReturn(true);

            // DB는 DESC(최신→오래된)로 반환
            Message msg3 = message(30L, directRoom, userB, "세 번째");
            Message msg2 = message(20L, directRoom, userB, "두 번째");
            Message msg1 = message(10L, directRoom, userB, "첫 번째");
            given(messageRepository.findByChatRoomId(10L, PageRequest.of(0, 30)))
                    .willReturn(List.of(msg3, msg2, msg1)); // DESC

            List<MessageResponse> result = chatService.getMessages(10L, "a@test.com", 0);

            // 응답은 ASC: id 10 → 20 → 30
            assertThat(result).extracting(MessageResponse::id)
                    .containsExactly(10L, 20L, 30L);
        }

        @Test
        @DisplayName("메시지가 1개여도 정상 반환")
        void 메시지_1개() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(directRoom, userA)).willReturn(true);

            Message msg = message(1L, directRoom, userB, "hello");
            given(messageRepository.findByChatRoomId(10L, PageRequest.of(0, 30)))
                    .willReturn(List.of(msg));

            List<MessageResponse> result = chatService.getMessages(10L, "a@test.com", 0);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).content()).isEqualTo("hello");
        }

        @Test
        @DisplayName("메시지가 없으면 빈 리스트 반환")
        void 메시지_없으면_빈리스트() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(directRoom, userA)).willReturn(true);
            given(messageRepository.findByChatRoomId(10L, PageRequest.of(0, 30))).willReturn(List.of());

            List<MessageResponse> result = chatService.getMessages(10L, "a@test.com", 0);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("채팅방 멤버가 아니면 메시지 조회 불가")
        void 권한없으면_예외() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(directRoom, userA)).willReturn(false);

            assertThatThrownBy(() -> chatService.getMessages(10L, "a@test.com", 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("접근 권한");
        }
    }

    // =========================================================================
    // addMember
    // =========================================================================

    @Nested
    @DisplayName("addMember")
    class AddMember {

        @Test
        @DisplayName("그룹 방에 새 멤버 정상 추가")
        void 정상추가() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(userRepository.findByEmail("c@test.com")).willReturn(Optional.of(userC));
            given(chatRoomRepository.findById(20L)).willReturn(Optional.of(groupRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(groupRoom, userA)).willReturn(true);
            given(chatRoomMemberRepository.existsByChatRoomAndUser(groupRoom, userC)).willReturn(false);
            given(chatRoomMemberRepository.findByChatRoom(groupRoom))
                    .willReturn(List.of(new ChatRoomMember(groupRoom, userA),
                                        new ChatRoomMember(groupRoom, userC)));
            given(messageRepository.countByChatRoomId(20L)).willReturn(0L);

            ChatRoomResponse response = chatService.addMember(20L, "a@test.com", "c@test.com");

            assertThat(response.memberEmails()).contains("c@test.com");
            then(chatRoomMemberRepository).should().save(any(ChatRoomMember.class));
        }

        @Test
        @DisplayName("DIRECT 방에는 멤버 추가 불가")
        void DIRECT방_예외() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));

            assertThatThrownBy(() -> chatService.addMember(10L, "a@test.com", "c@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("그룹 채팅방");
        }

        @Test
        @DisplayName("이미 참여 중인 유저는 추가 불가")
        void 이미참여중_예외() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(userRepository.findByEmail("b@test.com")).willReturn(Optional.of(userB));
            given(chatRoomRepository.findById(20L)).willReturn(Optional.of(groupRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(groupRoom, userA)).willReturn(true);
            given(chatRoomMemberRepository.existsByChatRoomAndUser(groupRoom, userB)).willReturn(true);

            assertThatThrownBy(() -> chatService.addMember(20L, "a@test.com", "b@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이미");
        }

        @Test
        @DisplayName("방 멤버가 아닌 사람은 초대 불가")
        void 권한없으면_예외() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(20L)).willReturn(Optional.of(groupRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(groupRoom, userA)).willReturn(false);

            assertThatThrownBy(() -> chatService.addMember(20L, "a@test.com", "c@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("접근 권한");
        }
    }

    // =========================================================================
    // leaveRoom
    // =========================================================================

    @Nested
    @DisplayName("leaveRoom")
    class LeaveRoom {

        @Test
        @DisplayName("방 나가기 정상 처리")
        void 정상나가기() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(directRoom, userA)).willReturn(true);

            chatService.leaveRoom(10L, "a@test.com");

            then(chatRoomMemberRepository).should().deleteByChatRoomAndUser(directRoom, userA);
        }

        @Test
        @DisplayName("참여하지 않은 방에서는 나가기 불가")
        void 참여안한방_예외() {
            given(userRepository.findByEmail("a@test.com")).willReturn(Optional.of(userA));
            given(chatRoomRepository.findById(10L)).willReturn(Optional.of(directRoom));
            given(chatRoomMemberRepository.existsByChatRoomAndUser(directRoom, userA)).willReturn(false);

            assertThatThrownBy(() -> chatService.leaveRoom(10L, "a@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("참여");
        }
    }

    // =========================================================================
    // ChatRoomMember.markAsRead (도메인 단위 테스트)
    // =========================================================================

    @Nested
    @DisplayName("ChatRoomMember.markAsRead 도메인 로직")
    class ChatRoomMemberMarkAsRead {

        @Test
        @DisplayName("null 상태에서 처음 읽으면 해당 ID로 설정")
        void 최초읽음() {
            ChatRoomMember member = new ChatRoomMember(directRoom, userA);
            assertThat(member.getLastReadMessageId()).isNull();

            member.markAsRead(10L);

            assertThat(member.getLastReadMessageId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("더 큰 ID로만 갱신됨")
        void 더큰ID만_갱신() {
            ChatRoomMember member = new ChatRoomMember(directRoom, userA);
            member.markAsRead(10L);
            member.markAsRead(20L);

            assertThat(member.getLastReadMessageId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("더 작은 ID로는 갱신 안 됨")
        void 더작은ID_무시() {
            ChatRoomMember member = new ChatRoomMember(directRoom, userA);
            member.markAsRead(20L);
            member.markAsRead(5L);

            assertThat(member.getLastReadMessageId()).isEqualTo(20L);
        }

        @Test
        @DisplayName("같은 ID로 다시 호출해도 유지")
        void 동일ID_유지() {
            ChatRoomMember member = new ChatRoomMember(directRoom, userA);
            member.markAsRead(10L);
            member.markAsRead(10L);

            assertThat(member.getLastReadMessageId()).isEqualTo(10L);
        }
    }

    // =========================================================================
    // helpers
    // =========================================================================

    private User user(Long id, String email) {
        User u = new User(email, "password");
        u.assignId(id);
        return u;
    }

    private ChatRoom chatRoom(Long id, ChatRoomType type, String name) {
        ChatRoom r = (name != null) ? new ChatRoom(type, name) : new ChatRoom(type);
        ReflectionTestUtils.setField(r, "id", id);
        return r;
    }

    private Message message(Long id, ChatRoom room, User sender, String content) {
        Message m = new Message(room, sender, content);
        ReflectionTestUtils.setField(m, "id", id);
        return m;
    }
}
