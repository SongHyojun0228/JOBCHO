package com.jobcho.user;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jobcho.alarm.AlarmRepository;
import com.jobcho.alarm.AlarmService;
import com.jobcho.alarm.Alarms;
import com.jobcho.member.MemberService;
import com.jobcho.workspace.Workspaces;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class UserIndexController {

	private final UserService userService;
	private final MemberService memberService;
	private final AlarmRepository alarmRepository;
	private final AlarmService alarmService;

	// 🌿 메인화면 페이지 GET
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

		Map<Integer, Integer> alarmCountMap = alarmService.getWorkspaceAlarmCountMap(user.getUserId());
	    model.addAttribute("alarmCountMap", alarmCountMap);

		return "user/index";
	}

	// 🌿 메인화면의 워크스페이스 알람 수 GET
	@GetMapping("/alarm/count")
	@ResponseBody
	public int getAlarmCount(Principal principal) {
		if (principal == null)
			return 0;

		Optional<Users> _user = this.userService.getUser(principal.getName());
		if (_user.isEmpty())
			return 0;

		Users user = _user.get();
		List<Alarms> alarms = this.alarmRepository.findByUserId(user.getUserId());
		return alarms.size();
	}
}
