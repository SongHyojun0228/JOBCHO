package com.jobcho.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/hello") // 클라가 app/hello로 보낸 메시지를 처리 (app은 디폴트, 헬로가 변동주소)
    @SendTo("/topic/greetings") // 응답을 /topic/greetings로 전송(거기있는 애들 전부 보이게함)
    public String greeting(String message) throws Exception {
        System.out.println("받음: " + message);
        return "테스트: " + message;
    }
    
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessage message) {
        messagingTemplate.convertAndSend(
            "/topic/chatroom/" + message.getChatroomId(), message
        );
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
}
