package com.jobcho.chatroom_member;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jobcho.chatroom.ChatroomService;
import com.jobcho.member.InviteToken;
import com.jobcho.member.InviteTokenRepository;
import com.jobcho.member.MemberService;
import com.jobcho.user.MailService;
import com.jobcho.user.UserService;
import com.jobcho.user.Users;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatroomMemberController {

	private final InviteTokenRepository inviteTokenRepository;
	private final ChatroomMemberService chatroomMemberService;
	private final ChatroomService chatroomService;
	private final MemberService memberService;
	private final UserService userService;
	private final MailService mailService;

    // 🌿 채팅방 멤버 초대하기 GET
	@GetMapping("workspace/invite/chat/member")
	public String getInviteChatMemeber(Principal principal, Model model) {
		List<Users> members = this.memberService.findUsersByWorkspaceId(1);
		model.addAttribute("members", members);

		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		model.addAttribute("user", user);
		return "workspace/invite_chat_member";
	}
	
	@GetMapping("/workspace/chatroom/invite/{chatroomId}/{token}")
	public String acceptInvite(@PathVariable("token") String token, @PathVariable("chatroomId") String chatroomId, Principal principal) {
		InviteToken inviteToken = inviteTokenRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("잘못된 초대 링크입니다."));

		if (inviteToken.getExpiredAt().isBefore(LocalDateTime.now())) {
			// return ResponseEntity.badRequest().body("초대 링크가 만료되었습니다.");
			System.out.println("초대 링크가 만료되었습니다.");
			return "error/404";
		}

		String inviteEmail = inviteToken.getInviteEmail();
		String userEmail = principal.getName();
		
		System.out.println("초대된 이메일 : " + inviteEmail + ", 로그인한 유저 이메일 : " + userEmail);

		if (!inviteEmail.equals(userEmail)) {
			// return ResponseEntity.status(HttpStatus.FORBIDDEN).body("초대된 사용자가 아닙니다.");
			System.out.println("초대된 사용자가 아닙니다.");
			return "error/404";
		}

		this.chatroomMemberService.addMember(inviteToken.getWorkspaceId(), userEmail);
		inviteTokenRepository.delete(inviteToken);

		// return ResponseEntity.ok("초대가 수락되었습니다! 워크스페이스로 이동합니다.");
		System.out.println("초대가 수락되었습니다. 채팅창으로 이동합니다.");
		return "redirect:/workspace/1/" + chatroomId;
	}

    // 🌿 채팅방 멤버 초대하기 POST
	@PostMapping("/{workspaceId}/{chatroomId}/invite")
	public String inviteChatMembers(@PathVariable("workspaceId") Integer workspaceId,
	                                @PathVariable("chatroomId") Integer chatroomId,
	                                @RequestParam("inviteMembers") List<Integer> userIds) {
	    String chatroomName = this.chatroomService.getChatroomNameById(chatroomId);

	    for (Integer userId : userIds) {
	        String email = this.userService.getEmailById(userId); // 유저 ID로 이메일 가져오기
	        mailService.sendChatroomInviteMail(email, chatroomId, chatroomName);
	        System.out.println("채팅방 초대 메일 전송 완료 : " + email);
	    }

	    return "redirect:/workspace/" + workspaceId + "/" + chatroomId;
	}

}
