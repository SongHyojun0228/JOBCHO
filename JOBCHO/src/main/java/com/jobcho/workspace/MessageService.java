package com.jobcho.workspace;

import java.util.List;

import org.springframework.stereotype.Service;

import com.jobcho.websocket.ChatMessage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MessageService {
	private final MessageRepository messageRepository;
	
	public void create(ChatMessage msg) {
		Messages m = new Messages();
		m.setChatroomId(msg.getChatroomId());
		m.setSenderId(msg.getSenderId());
		m.setContent(msg.getContent());
		m.setIsEdited(0);
		m.setIsDeleted(0);
		this.messageRepository.save(m);
	}
	
	public List<Messages> getMessage(int chatroomId){
		List<Messages> messages = messageRepository.findByChatroomId(chatroomId);
		return messages;
	}
}
