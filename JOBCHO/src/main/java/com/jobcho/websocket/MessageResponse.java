package com.jobcho.websocket;

import java.time.LocalDateTime;
import java.util.List;

import com.jobcho.message.Messages;
import com.jobcho.user.Users;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageResponse {
    private Integer messageId;
    private String sender;
    private String content;
    private Integer chatroomId;
    private LocalDateTime createdDate;
    private List<String> mentions;  
    private String fileName;

    public MessageResponse(Integer messageId, String sender, String content,
                           Integer chatroomId, List<String> mentions, String files) {
        this.messageId = messageId;
        this.sender = sender;
        this.content = content;
        this.chatroomId = chatroomId;
        this.mentions = mentions;
        this.fileName = files;
    }

}
