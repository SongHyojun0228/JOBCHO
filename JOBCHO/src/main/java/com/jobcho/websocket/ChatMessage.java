package com.jobcho.websocket;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
	private String sender;
    private String content;
    private Integer chatroomId;
    private Integer senderId;
}
