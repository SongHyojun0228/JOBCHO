package com.jobcho;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@GetMapping("/")
	public String redirectToSignup(Principal principal) {
		return "redirect:/index";
	}

}
