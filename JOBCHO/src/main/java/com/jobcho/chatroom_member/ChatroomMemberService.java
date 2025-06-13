package com.jobcho.chatroom_member;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.jobcho.chatroom.ChatroomRepository;
import com.jobcho.chatroom.Chatrooms;
import com.jobcho.user.UserRepository;
import com.jobcho.user.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatroomMemberService {

	private final UserRepository userRepository;
	private final ChatroomMemberRepository chatroomMemberRepository;
	private final ChatroomRepository chatroomRepository;

    // 🌿 채팅방 멤버 추가하기 메서드
	public void addMember(Integer chatroomId, String email) {
		Optional<Chatrooms> _chatroom = this.chatroomRepository.findById(chatroomId);
		Chatrooms chatroom = _chatroom.get();
		Optional<Users> _user = this.userRepository.findByUserEmail(email);
		Users user = _user.get();

		if (!chatroomMemberRepository.existsByChatroomAndUser(chatroom, user)) {
			System.out.println("채팅 멤버 추가 : " + user.getUserEmail() + ", " + user.getUserName());

			ChatroomMember chatroomMember = new ChatroomMember();
			chatroomMember.setChatroom(chatroom);
			chatroomMember.setUser(user);
			this.chatroomMemberRepository.save(chatroomMember);
		} else {
			System.out.println("해당 유저는 해당 채팅방에 이미 존재");
		}
	}

    // 🌿 채팅방아이디로 해당 채팅방 멤버 불러오기 메서드
	public List<ChatroomMember> getChatroomMembersByChatroomId(Integer id) {
		System.out.println("Chatroom ID: " + id);
	    List<ChatroomMember> members = this.chatroomMemberRepository.findByChatroom_ChatroomId(id);
	    System.out.println("Members found: " + members.size());
	    return members;
	}

}
