package com.jobcho.git;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class ProjectManageController {

	@GetMapping("workspace/{workspaceId}/project")
	public String projectMain(@PathVariable("workspaceId") int workspaceId, Model model, Principal principal) {

		return "ProjectManage/projectmanage";
	}
}
