package com.jobcho.member;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jobcho.user.MailService;
import com.jobcho.workspace.WorkspaceService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MemberController {

	private final InviteTokenRepository inviteTokenRepository;
	private final MemberService memberSerivce;
	private final MailService mailService;
	private final WorkspaceService workspaceService;

	@GetMapping("/workspace/invite/{token}")
	public ResponseEntity<String> acceptInvite(@PathVariable("token") String token, Principal principal) {
		InviteToken inviteToken = inviteTokenRepository.findByToken(token)
				.orElseThrow(() -> new IllegalArgumentException("잘못된 초대 링크입니다."));

		if (inviteToken.getExpiredAt().isBefore(LocalDateTime.now())) {
			return ResponseEntity.badRequest().body("초대 링크가 만료되었습니다.");
		}

		String inviteEmail = inviteToken.getInviteEmail();
		String userEmail = principal.getName();
		
		System.out.println("초대된 이메일 : " + inviteEmail + ", 로그인한 유저 이메일 : " + userEmail);

		if (!inviteEmail.equals(userEmail)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("초대된 사용자가 아닙니다.");
		}

		memberSerivce.addMember(inviteToken.getWorkspaceId(), userEmail);

		inviteTokenRepository.delete(inviteToken);

		return ResponseEntity.ok("초대가 수락되었습니다! 워크스페이스로 이동합니다.");
	}

	@GetMapping("/workspace/invite")
	public String getInviteMember() {
		return "workspace/invite_member";
	}

	@PostMapping("/{workspaceId}/invite")
	public ResponseEntity<?> inviteMember(@PathVariable("workspaceId") Integer workspaceId,
			@RequestParam("email") String email) {
		String workspaceName = workspaceService.getWorkspaceNameById(workspaceId);
		mailService.sendInviteMail(email, workspaceId, workspaceName);
		return ResponseEntity.ok("초대 메일 전송 완료");
	}

}
