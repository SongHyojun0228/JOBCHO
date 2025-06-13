package com.jobcho.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jobcho.chatroom_member.ChatroomMember;
import com.jobcho.chatroom_member.ChatroomMemberDTO;
import com.jobcho.chatroom_member.ChatroomMemberService;
import com.jobcho.mention.MentionService;
import com.jobcho.message.MessageService;
import com.jobcho.message.MymessageService;
import com.jobcho.user.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@ResponseBody
public class ChatController {

	private final MessageService messageService;
	private final ChatroomMemberService chatroomMemberService;
	private final UserService userService;
	private final MentionService mentionService;
	private final MymessageService mymessageService;

	@GetMapping("/chatroom/{id}/members")
	public List<ChatroomMemberDTO> getChatroomMembers(@PathVariable("id") Integer id) {
		List<ChatroomMember> members = chatroomMemberService.getChatroomMembersByChatroomId(id);

		return members.stream().map(ChatroomMemberDTO::new).collect(Collectors.toList());
	}

	// 🌿 채팅방 메세지 전송
	@MessageMapping("/chat.sendMessage")
	public void sendMessage(ChatMessage message) {
		System.out.println(">>> incoming message: " + message);
		this.messageService.create(message);

		List<String> validMentions = new ArrayList<>();

		if (message.getMentions() != null) {
			for (String mentionUserName : message.getMentions()) {
				System.out.println(">>> mention user: " + mentionUserName);
				Integer receiverId = userService.findUserIdByUserName(mentionUserName);
				if (receiverId != null) {
					mentionService.saveMention(message.getChatroomId(), message.getSenderId(), receiverId);
					System.out.println("<<< mention save2 >>>");
					validMentions.add(mentionUserName);
				}
			}
		}

		message.setMentions(validMentions);

		messagingTemplate.convertAndSend("/topic/chatroom/" + message.getChatroomId(), message);
	}
	// 🌿 나와의 채팅방 메세지 전송
	@MessageMapping("/chat.sendMymessage")
	public void sendMymessage(ChatMessage message) {
		System.out.println("마이챗 호출됨");
		this.mymessageService.create(message);
		messagingTemplate.convertAndSend("/topic/chatroom/mychat/" + message.getChatroomId(), message);
	}

	@Autowired
	private SimpMessagingTemplate messagingTemplate;
}
