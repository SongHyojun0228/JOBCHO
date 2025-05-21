package com.jobcho.user;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserSettingController {

	private final UserService userService;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/setting")
	public String getUserSetting(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/user/login";
		}

		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		model.addAttribute("user", user);

		return "user/user_setting";
	}
	
	@PostMapping("/username/change")
	public String changeUserName(@RequestParam("new_user_name") String newUserName, Principal principal) {
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();

		try {
			this.userService.changeUserName(user, newUserName);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("이름 변경 중 예외 발생");
		}

		System.out.println("이름 변경 성공");
		return "redirect:/user/setting";
	}

	@PostMapping("/useremail/change")
	public String changeUserEmail(@RequestParam("new_user_email") String newUserEmail, Principal principal,
			Model model) {

		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		model.addAttribute("user", user);

		if (!newUserEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
			model.addAttribute("error", "이메일 형식이 올바르지 않습니다.");
			return "user/user_setting";
		}

		if (userService.existsByEmail(newUserEmail)) {
			model.addAttribute("error", "이미 사용 중인 이메일입니다.");
			return "user/user_setting";
		}

		try {
			this.userService.changeUserEmail(user, newUserEmail);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("이메일 변경 중 예외 발생");
		}

		System.out.println("이메일 변경 성공");
		return "redirect:/user/setting";
	}

	@PostMapping("/userpassword/change")
	public String changePassword(@RequestParam("user_password") String userPassword,
			@RequestParam("new_user_password") String newUerPassword, Principal principal) {
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();

		if (!passwordEncoder.matches(userPassword, user.getUserPassword())) {
			System.out.println("비밀번호 변경 시 : 현재 비밀번호 틀림");
			return "user/user_setting";
		}

		try {
			this.userService.changePassword(user, newUerPassword);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("비밀번호 변경 중 예외 발생");
		}

		System.out.println("비밀번호 변경 성공");
		return "redirect:/user/logout";
	}

	@PostMapping("/account/delete")
	public String deleteUser(Principal principal) {
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		this.userService.deleteUser(user);

		return "redirect:/user/logout";
	}

}
