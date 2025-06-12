package com.jobcho.chatroom_member;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobcho.chatroom.Chatrooms;
import com.jobcho.user.Users;

public interface ChatroomMemberRepository extends JpaRepository<ChatroomMember, Integer> {
	boolean existsByChatroomAndUser(Chatrooms chatroom, Users user);
	
	List<ChatroomMember> findByChatroom_ChatroomId(Integer chatroomId);
}
