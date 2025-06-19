package com.jobcho.websocket;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
	private String sender;
	private String receiver;
	private String content;
	private Integer chatroomId;
	private Integer senderId;
	private Integer receiverId;
	private String profile;
	private String fileName;

	private List<String> mentions;
}
