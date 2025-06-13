package com.jobcho.function;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jobcho.user.UserService;
import com.jobcho.user.Users;

@Controller
@RequestMapping("/function")
public class CalendarController {

	private final UserService userService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public CalendarController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/calendar")
	public String showCalendar(Model model) {
		String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

		if (userEmail == null || userEmail.equals("anonymousUser")) {
			return "redirect:/index";
		}
		Users user = userService.getUser(userEmail).orElse(null);

		model.addAttribute("user", user);
		return "function/calendar";
	}

	@GetMapping("/calendar/events")
	@ResponseBody
	public List<Map<String, Object>> getUserSchedules() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		Users user = userService.getUser(email).orElse(null);
		if (user == null) {
			return Collections.emptyList();
		}
		Integer userId = user.getUserId();
		
		String sql = ""
		        + "SELECT "
		        + "  s.SCHEDULE_ID    AS \"id\", "
		        + "  s.TITLE          AS \"title\", "
		        + "  s.DESCRIPTION    AS \"description\", "
		        + "  s.COLOR          AS \"color\", "
		        + "  s.START_DATE     AS \"startDate\", "
		        + "  s.END_DATE       AS \"endDate\", "
		        + "  s.CHECK_REPEAT   AS \"checkRepeat\", "
		        + "  COALESCE(u.USER_NAME, w.WORKSPACE_NAME, '작성자 미확인') AS \"writer\" "
		        + "FROM SCHEDULES s "
		        + "LEFT JOIN USERS u       ON s.USER_ID      = u.USER_ID "
		        + "LEFT JOIN WORKSPACES w  ON s.WORKSPACE_ID = w.WORKSPACE_ID "
		        + "WHERE s.USER_ID = ?";

		System.out.println("[getUserSchedules SQL] " + sql + " / params: " + userId);

		return jdbcTemplate.queryForList(sql, userId);

	}

	@PostMapping("/calendar/events")
	@ResponseBody
	public Map<String, Object> createSchedule(@RequestBody Map<String, Object> req) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		Integer userId = userService.getUser(email).orElseThrow().getUserId();

		String title = (String) req.get("title");
		String description = (String) req.get("description");
		String color = (String) req.get("color");
		
		Instant startInstant = Instant.parse((String) req.get("startDate"));
		Instant   endInstant   = Instant.parse((String) req.get("endDate"));
		Timestamp start        = Timestamp.from(startInstant);
		Timestamp end          = Timestamp.from(endInstant);
		
		Integer checkRepeat = (Integer) req.get("checkRepeat");

		String sql = "" + "INSERT INTO SCHEDULES ("
				+ "\tSCHEDULE_ID, USER_ID, TITLE, DESCRIPTION, COLOR, START_DATE, END_DATE, CHECK_REPEAT"
				+ ") VALUES ("
				+ "\tSEQ_SCHEDULE.NEXTVAL, ?, ?, ?, ?, ?, ?, ?" + ")";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(con -> {
			PreparedStatement ps = con.prepareStatement(sql, new String[] { "SCHEDULE_ID" });
			ps.setInt(1, userId);
			ps.setString(2, title);
			ps.setString(3, description);
			ps.setString(4, color);
			ps.setTimestamp(5, start);
			ps.setTimestamp(6, end);
			ps.setInt(7, checkRepeat);
			return ps;

		}, keyHolder);

		Map<String, Object> resp = new HashMap<>();
		resp.put("id", keyHolder.getKey().longValue());
		return resp;
	}
	
	@DeleteMapping("/calendar/events/{id}")
	@ResponseBody
	public Map<String, String> deleteSchedule(@PathVariable("id") Long scheduleId) {
		String sql = "DELETE FROM SCHEDULES WHERE SCHEDULE_ID = ?";
		int rows = jdbcTemplate.update(sql, scheduleId);
		
		Map<String, String> resp = new HashMap<>();
	    if (rows > 0) {
	        resp.put("status", "deleted");
	    } else {
	        resp.put("status", "not_found");
	    }
		return resp;
	}

}
