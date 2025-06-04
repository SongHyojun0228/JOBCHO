package com.jobcho.chatroom.member;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobcho.user.Users;
import com.jobcho.workspace.Chatrooms;

public interface ChatroomMemberRepository extends JpaRepository<ChatroomMember, Integer> {
	boolean existsByChatroomAndUser(Chatrooms chatroom, Users user);
}
