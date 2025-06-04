package com.jobcho.function;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/function")
public class CalendarController {
	
	@GetMapping("/calendar")
	public String CalenderShow() {
		return "function/calendar";
	}
	
}
