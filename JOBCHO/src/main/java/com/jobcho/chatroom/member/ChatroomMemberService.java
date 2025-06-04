package com.jobcho.chatroom.member;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.jobcho.user.UserRepository;
import com.jobcho.user.Users;
import com.jobcho.workspace.ChatroomRepository;
import com.jobcho.workspace.Chatrooms;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatroomMemberService {

	private final UserRepository userRepository;
	private final ChatroomMemberRepository chatroomMemberRepository;
	private final ChatroomRepository chatroomRepository;

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
			System.out.println("해당 유저는 해당 채팅방에	 이미 존재");
		}
	}

}
