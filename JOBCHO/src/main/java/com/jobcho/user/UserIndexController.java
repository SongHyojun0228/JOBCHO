package com.jobcho.user;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.jobcho.member.MemberService;
import com.jobcho.workspace.Workspaces;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserIndexController {

	private final UserService userService;
	private final MemberService memberService;

	@GetMapping("/index")
	public String getIndex(Principal principal, Model model) {
		if (principal == null) {
			return "user/login";
		}

		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		model.addAttribute("user", user);

		List<Workspaces> workspaces = this.memberService.findWorkspacesByUserUserId(user.getUserId());
		model.addAttribute("workspaces", workspaces);
		
		System.out.println("userId = " + user.getUserId());
		System.out.println("workspaces.size() : " + workspaces.size());

		return "user/index";
	}
}
