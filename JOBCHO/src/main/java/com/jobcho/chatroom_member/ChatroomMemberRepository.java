package com.jobcho.chatroom_member;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jobcho.chatroom.Chatrooms;
import com.jobcho.user.Users;

public interface ChatroomMemberRepository extends JpaRepository<ChatroomMember, Integer> {
	boolean existsByChatroomAndUser(Chatrooms chatroom, Users user);
	
	List<ChatroomMember> findByChatroom_ChatroomId(Integer chatroomId);
	
	@Query("SELECT m.user FROM ChatroomMember m WHERE m.chatroom.chatroomId = :chatroomId ORDER BY m.user.userName ASC")
	List<Users> findUsersByWorkspaceId(@Param("chatroomId") Integer chatroomId);
}
