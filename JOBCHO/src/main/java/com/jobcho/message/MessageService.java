package com.jobcho.message;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.jobcho.websocket.ChatMessage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MessageService {

	private final MessageRepository messageRepository;

	// 🌿 채팅방 메세지 생성 메서드
	public void create(ChatMessage msg) {
		Messages m = new Messages();
		m.setChatroomId(msg.getChatroomId());
		m.setSenderId(msg.getSenderId());
		m.setContent(msg.getContent());
		m.setIsEdited(0);
		m.setIsDeleted(0);
		this.messageRepository.save(m);
	}

	// 🌿 채팅방 메세지 불러오기 메서드
	public List<Messages> getMessage(int chatroomId) {
		List<Messages> messages = messageRepository.findByChatroomId(chatroomId);
		return messages;
	}

	// 🌿 채팅방 메세지 삭제 메서드
	public void deleteMessage(int messageId) {
		Optional<Messages> optionalMessage = messageRepository.findById(messageId);
		if (optionalMessage.isPresent()) {
			Messages m = optionalMessage.get();
			m.setIsDeleted(1);
			messageRepository.save(m);
		}
	}

	// 🌿 채팅방 메세지 수정 메서드
	public void updateMessage(int messageId, String newContent) {
		Optional<Messages> optionalMessage = messageRepository.findById(messageId);

		if (optionalMessage.isPresent()) {
			Messages message = optionalMessage.get();
			message.setContent(newContent);
			message.setIsEdited(1);
			messageRepository.save(message);
		}
	}

	// 🌿 답글 객체 불러오기 메서드
	public Messages getMessageWithMessageId(int messageId) {
		Optional<Messages> message = this.messageRepository.findById(messageId);
		if (message.isPresent()) {
			return message.get();
		} else {
			return message.orElse(new Messages());
		}
	}

	// 🌿 답글 작성 메서드
	public void addReply(int chatroomId, int parentId, String content, int senderId) {
		Messages parent = messageRepository.findById(parentId).orElseThrow();
		Messages reply = new Messages();
		reply.setChatroomId(chatroomId);
		reply.setSenderId(senderId);
		reply.setContent(content);
		reply.setIsDeleted(0);
		reply.setIsEdited(0);
		reply.setParentMessage(parent);
		messageRepository.save(reply);
	}

	// 🌿 답글 객체 불러오기 메서드
	public List<Messages> getReplies(int parentMessageId) {
		return messageRepository.findByParentMessage_MessageIdOrderByCreatedDateAsc(parentMessageId);
	}

}
