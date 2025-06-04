package com.jobcho.workspace;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatroomService {
	
	private final ChatroomRepository chatroomRepository;

	public String getChatroomNameById(Integer chatroomId) {
		Chatrooms chatroom = this.chatroomRepository.getById(chatroomId);
		return chatroom.getChatroomName();
	}
}
