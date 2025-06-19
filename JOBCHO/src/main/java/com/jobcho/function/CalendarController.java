package com.jobcho.function;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RequestParam;
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
		if (userEmail == null || "anonymousUser".equals(userEmail)) {
			return "redirect:/index";
		}

		Users user = userService.getUser(userEmail).orElse(null);
		if (user == null) {
			return "redirect:/index";
		}

		model.addAttribute("user", user);

		Integer userId = user.getUserId();

		StringBuilder wsSb = new StringBuilder().append("SELECT ").append("  w.WORKSPACE_ID   AS workspaceId, ")
				.append("  w.WORKSPACE_NAME AS workspaceName, ").append("  NVL(vs.VISIBLE, 'Y') AS visible, ")
				.append("  vs.COLOR          AS color ").append("FROM MEMBERS m ")
				.append("JOIN WORKSPACES w ON m.WORKSPACE_ID = w.WORKSPACE_ID ")
				.append("LEFT JOIN WORKSPACE_VIEW_SETTINGS vs ").append("  ON vs.USER_ID = m.USER_ID ")
				.append(" AND vs.WORKSPACE_ID = w.WORKSPACE_ID ").append("WHERE m.USER_ID = ?");

		List<Map<String, Object>> workspaces = jdbcTemplate.queryForList(wsSb.toString(), userId);
		model.addAttribute("workspaces", workspaces);

		Integer workspaceId = null;
		if (!workspaces.isEmpty()) {
			Object wsObj = workspaces.get(0).get("workspaceId");
			if (wsObj instanceof Number) {
				workspaceId = ((Number) wsObj).intValue();
			}
		}
		model.addAttribute("workspaceId", workspaceId);

		return "function/calendar";
	}

	@PostMapping("/workspace-settings")
	@ResponseBody
	public ResponseEntity<Void> updateWorkspaceSettings(@RequestBody Map<String, Object> payload) {
		Integer workspaceId = ((Number) payload.get("workspaceId")).intValue();

		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		Users user = userService.getUser(email).orElseThrow(() -> new IllegalArgumentException("사용자 정보 없음: " + email));
		Integer userId = user.getUserId();

		String visible = (String) payload.get("visible");
		String color = (String) payload.get("color");

		String mergeSql = new StringBuilder().append("MERGE INTO WORKSPACE_VIEW_SETTINGS t ")
				.append("USING (SELECT ? AS USER_ID, ? AS WORKSPACE_ID FROM DUAL) src ")
				.append("ON (t.USER_ID = src.USER_ID AND t.WORKSPACE_ID = src.WORKSPACE_ID) ")
				.append("WHEN MATCHED THEN ").append("  UPDATE SET t.VISIBLE = ?, t.COLOR = ? ")
				.append("WHEN NOT MATCHED THEN ").append("  INSERT (USER_ID, WORKSPACE_ID, VISIBLE, COLOR) ")
				.append("  VALUES (src.USER_ID, src.WORKSPACE_ID, ?, ?)").toString();

		jdbcTemplate.update(mergeSql, userId, workspaceId, visible, color, visible, color);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/workspace-settings")
	@ResponseBody
	public ResponseEntity<List<Map<String, Object>>> getWorkspaceSettings() {
		Users user = userService.getUser(SecurityContextHolder.getContext().getAuthentication().getName())
				.orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
		Integer userId = user.getUserId();
		String sql = "SELECT WORKSPACE_ID, VISIBLE, COLOR FROM WORKSPACE_VIEW_SETTINGS WHERE USER_ID = ?";
		List<Map<String, Object>> settings = jdbcTemplate.queryForList(sql, userId);
		return ResponseEntity.ok(settings);
	}

	@GetMapping("/calendar/view-settings")
	@ResponseBody
	public List<Map<String, Object>> getViewSettings() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		Users user = userService.getUser(email).orElseThrow();
		Integer userId = user.getUserId();

		String sql = "SELECT WORKSPACE_ID, VISIBLE, COLOR " + "  FROM WORKSPACE_VIEW_SETTINGS " + " WHERE USER_ID = ?";
		return jdbcTemplate.queryForList(sql, userId);
	}

	@PostMapping("/calendar/view-settings")
	@ResponseBody
	public void saveViewSettings(@RequestBody List<Map<String, Object>> settings) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		Users user = userService.getUser(email).orElseThrow();
		Integer userId = user.getUserId();

		jdbcTemplate.update("DELETE FROM WORKSPACE_VIEW_SETTINGS WHERE USER_ID = ?", userId);

		String insertSql = "INSERT INTO WORKSPACE_VIEW_SETTINGS " + "(USER_ID, WORKSPACE_ID, VISIBLE, COLOR) "
				+ "VALUES (?, ?, ?, ?)";
		for (Map<String, Object> m : settings) {
			Integer wsId = (Integer) m.get("workspaceId");
			String vis = (Boolean) m.get("visible") ? "Y" : "N";
			String color = (String) m.get("color");
			jdbcTemplate.update(con -> {
				PreparedStatement ps = con.prepareStatement(insertSql);
				ps.setInt(1, userId);
				ps.setInt(2, wsId);
				ps.setString(3, vis);
				ps.setString(4, color);
				return ps;
			});
		}
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

		List<Integer> workspaceIds = jdbcTemplate.queryForList("SELECT WORKSPACE_ID FROM MEMBERS WHERE USER_ID = ?",
				Integer.class, userId);

		StringBuilder sql = new StringBuilder().append("SELECT ").append("  s.SCHEDULE_ID   AS \"id\", ")
				.append("  s.TITLE         AS \"title\", ").append("  s.DESCRIPTION   AS \"description\", ")
				.append("  s.COLOR         AS \"color\", ").append("  s.START_DATE    AS \"startDate\", ")
				.append("  s.END_DATE      AS \"endDate\", ").append("  s.CHECK_REPEAT  AS \"checkRepeat\", ")
				.append("  s.USER_ID       AS \"userId\", ").append("  s.WORKSPACE_ID  AS \"workspaceId\", ")
				.append("  COALESCE(u.USER_NAME, w.WORKSPACE_NAME, '작성자 미확인') AS \"writer\" ")
				.append("FROM SCHEDULES s ").append("LEFT JOIN USERS u     ON s.USER_ID      = u.USER_ID ")
				.append("LEFT JOIN WORKSPACES w ON s.WORKSPACE_ID = w.WORKSPACE_ID ")
				.append("LEFT JOIN WORKSPACE_VIEW_SETTINGS v ")
				.append("  ON v.USER_ID = ? AND v.WORKSPACE_ID = s.WORKSPACE_ID ")
				.append("WHERE COALESCE(v.VISIBLE,'Y') = 'Y' ").append("  AND (s.USER_ID = ?");

		if (!workspaceIds.isEmpty()) {
			sql.append(" OR s.WORKSPACE_ID IN (")
					.append(String.join(",", Collections.nCopies(workspaceIds.size(), "?"))).append(")");
		}
		sql.append(")");

		List<Object> params = new ArrayList<>();
		params.add(userId);
		params.add(userId);
		params.addAll(workspaceIds);

		List<Map<String, Object>> schedules = jdbcTemplate.queryForList(sql.toString(), params.toArray());

		for (Map<String, Object> sch : schedules) {
			Long id = ((Number) sch.get("id")).longValue();

			List<Timestamp> deletes = jdbcTemplate.queryForList("SELECT exception_date FROM schedule_exception "
					+ "WHERE schedule_id = ? AND exception_type = 'DELETE'", Timestamp.class, id);
			List<String> exceptionDates = new ArrayList<>();
			for (Timestamp ts : deletes) {
				exceptionDates.add(ts.toInstant().toString());
			}
			sch.put("exceptionDates", exceptionDates);

			Timestamp after = jdbcTemplate.queryForObject("SELECT MIN(exception_date) FROM schedule_exception "
					+ "WHERE schedule_id = ? AND exception_type = 'DELETE_AFTER'", Timestamp.class, id);
			String repeatEnd = (after != null) ? after.toInstant().toString() : null;
			sch.put("repeatEnd", repeatEnd);
		}

		return schedules;
	}

	@PostMapping("/calendar/events")
	@ResponseBody
	public Map<String, Object> createSchedule(@RequestBody Map<String, Object> req) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		Integer userId = userService.getUser(email).orElseThrow().getUserId();

		String title = (String) req.get("title");
		String description = (String) req.get("description");
		String color = (String) req.get("color");
		Instant startI = Instant.parse((String) req.get("startDate"));
		Instant endI = Instant.parse((String) req.get("endDate"));
		Timestamp start = Timestamp.from(startI);
		Timestamp end = Timestamp.from(endI);

		// Integer checkRepeat = (Integer) req.get("checkRepeat");

		Number repeatNum = (Number) req.get("checkRepeat");
		int checkRepeat = repeatNum != null ? repeatNum.intValue() : 0;

		String sql = new StringBuilder().append("INSERT INTO SCHEDULES (")
				.append("  SCHEDULE_ID, USER_ID, TITLE, DESCRIPTION,")
				.append("  COLOR, START_DATE, END_DATE, CHECK_REPEAT").append(") VALUES (")
				.append("  SEQ_SCHEDULE.NEXTVAL, ?, ?, ?, ?, ?, ?, ?").append(")").toString();

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
	public ResponseEntity<Map<String, String>> deleteSchedule(@PathVariable("id") Long scheduleId,
			@RequestParam(value = "instanceDate", required = false) @DateTimeFormat(iso = ISO.DATE_TIME) LocalDateTime instanceDate) {

		Integer checkRepeat = jdbcTemplate.queryForObject("SELECT CHECK_REPEAT FROM SCHEDULES WHERE SCHEDULE_ID = ?",
				Integer.class, scheduleId);

		if (instanceDate == null || checkRepeat == null || checkRepeat == 0) {
			int rows = jdbcTemplate.update("DELETE FROM SCHEDULES WHERE SCHEDULE_ID = ?", scheduleId);
			String status = rows > 0 ? "deleted" : "not_found";
			return ResponseEntity.ok(Collections.singletonMap("status", status));
		}

		String sql = new StringBuilder().append("INSERT INTO schedule_exception ")
				.append("(exception_id, schedule_id, exception_date, exception_type) ")
				.append("VALUES (seq_schedule_exception.nextval, ?, ?, 'DELETE')").toString();
		jdbcTemplate.update(sql, scheduleId, Timestamp.valueOf(instanceDate));
		return ResponseEntity.ok(Collections.singletonMap("status", "deleted"));
	}

	@PostMapping("/calendar/events/{id}/delete-after")
	@ResponseBody
	public Map<String, String> deleteFutureInstances(@PathVariable("id") Long scheduleId,
			@RequestParam("from") String from) {
		String sql = "" + "INSERT INTO schedule_exception "
				+ "(exception_id, schedule_id, exception_date, exception_type) "
				+ "VALUES (seq_schedule_exception.nextval, ?, ?, 'DELETE_AFTER')";
		jdbcTemplate.update(sql, scheduleId, Timestamp.from(Instant.parse(from)));
		return Collections.singletonMap("status", "deleted");
	}

	@GetMapping("/calendar/groups")
	@ResponseBody
	public List<Map<String, Object>> getUserAndTeams() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		Users user = userService.getUser(email).orElseThrow(() -> new IllegalArgumentException("사용자 정보 없음: " + email));
		Integer userId = user.getUserId();

		List<Map<String, Object>> groups = new ArrayList<>();

		Map<String, Object> self = new HashMap<>();
		self.put("type", "user");
		self.put("id", userId);
		self.put("name", user.getUserName());
		groups.add(self);

		String sql = new StringBuilder().append("SELECT w.WORKSPACE_ID, w.WORKSPACE_NAME ").append("FROM MEMBERS m ")
				.append("JOIN WORKSPACES w ").append("  ON m.WORKSPACE_ID = w.WORKSPACE_ID ")
				.append("WHERE m.USER_ID = ?").toString();
		List<Map<String, Object>> teams = jdbcTemplate.queryForList(sql, userId);

		for (Map<String, Object> t : teams) {
			Map<String, Object> m = new HashMap<>();
			m.put("type", "team");
			m.put("id", t.get("WORKSPACE_ID"));
			m.put("name", t.get("WORKSPACE_NAME"));
			groups.add(m);
		}
		return groups;

	}

}