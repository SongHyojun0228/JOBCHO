package com.jobcho.workspace;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jobcho.alarm.AlarmRepository;
import com.jobcho.alarm.AlarmService;
import com.jobcho.alarm.Alarms;
import com.jobcho.bookmark.BookmarkService;
import com.jobcho.bookmark.Bookmarks;
import com.jobcho.chatroom.ChatroomService;
import com.jobcho.chatroom.Chatrooms;
import com.jobcho.folder.FolderService;
import com.jobcho.folder.Folders;
import com.jobcho.member.MemberService;
import com.jobcho.message.MessageService;
import com.jobcho.message.Messages;
import com.jobcho.mychatroom.MyChatroom;
import com.jobcho.mychatroom.MyChatroomService;
import com.jobcho.notification.NotificationDTO;
import com.jobcho.notification.NotificationService;
import com.jobcho.notification.Notifications;
import com.jobcho.task.TaskDto;
import com.jobcho.task.TaskService;
import com.jobcho.task.Tasks;
import com.jobcho.user.UserService;
import com.jobcho.user.Users;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class WorkspaceSideBarController {

	private final FolderService folderService;
	private final NotificationService notificationService;
	private final BookmarkService bookmarkService;
	private final WorkspaceService workspaceService;
	private final TaskService taskService;
	private final UserService userService;
	private final MemberService memberService;
	private final MessageService messageService;
	private final AlarmRepository alarmRepository;
	private final AlarmService alarmService;
	private final MyChatroomService myChatroomService;
	private final ChatroomService chatroomService;

	// 🌿 사이드 바 관련 컨트롤러 메서드
	// 🌿 사이드바 할 일 자세히 보기 GET
	@GetMapping("/workspace/{workspaceId}/{chatroomId}/side/{taskId}")
	public String workspaceMain_SidebarTask(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId, @PathVariable("taskId") int taskId, Model model,
			Principal principal) {

		List<Folders> folders = workspaceService.getFolderWithChatrooms(workspaceId);
		List<Tasks> tasks = workspaceService.getTask(chatroomId);
		List<Notifications> notifications = workspaceService.getNotifi(chatroomId);
		List<Messages> messages = messageService.getMessage(chatroomId);
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		MyChatroom mychat = myChatroomService.findMychatByUserID(user.getUserId());
		List<Bookmarks> bookmarks = bookmarkService.getBookmarksByUserId(user.getUserId());
		Set<Integer> bookmarkedChatroomIds = bookmarks.stream().map(Bookmarks::getChatroomId)
				.collect(Collectors.toSet());
		Set<Integer> bookmarkedMyChatroomIds = bookmarks.stream().map(Bookmarks::getMyChatroomId)
				.collect(Collectors.toSet());
		Set<Integer> bookmarkedMessageIds = bookmarks.stream().map(Bookmarks::getMessageId).collect(Collectors.toSet());
		Chatrooms chatrooms = this.workspaceService.getChatroomWithChatId(chatroomId);
		Tasks tasksDetail = this.workspaceService.getTaskWithTaskId(taskId);

		model.addAttribute("user", user);
		model.addAttribute("workspaceId", workspaceId);
		model.addAttribute("folders", folders);
		model.addAttribute("chatroomId", chatroomId);
		model.addAttribute("tasks", tasks);
		model.addAttribute("chatrooms", chatrooms);
		model.addAttribute("notifications", notifications);
		model.addAttribute("messages", messages);
		model.addAttribute("tasksDetail", tasksDetail);
		model.addAttribute("bookmarks", bookmarks);
		model.addAttribute("bookmarkedChatroomIds", bookmarkedChatroomIds);
		model.addAttribute("bookmarkedMyChatroomIds", bookmarkedMyChatroomIds);
		model.addAttribute("bookmarkedMessageIds", bookmarkedMessageIds);
		model.addAttribute("mychat", mychat);
		return "workspace/workspace_sidebar_taskDetail";
	}

	// 🌿 사이드바 채팅 수정 GET
	@GetMapping("/workspace/{workspaceId}/{chatroomId}/side/modifyChat")
	public String workspaceMain_SidebarChange(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId, Model model, Principal principal) {
		List<Folders> folders = workspaceService.getFolderWithChatrooms(workspaceId);
		List<Tasks> tasks = workspaceService.getTask(chatroomId);
		List<Notifications> notifications = workspaceService.getNotifi(chatroomId);
		List<Messages> messages = messageService.getMessage(chatroomId);
		Chatrooms chatrooms = this.workspaceService.getChatroomWithChatId(chatroomId);
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		List<Bookmarks> bookmarks = bookmarkService.getBookmarksByUserId(user.getUserId());
		Set<Integer> bookmarkedChatroomIds = bookmarks.stream().map(Bookmarks::getChatroomId)
				.collect(Collectors.toSet());
		Set<Integer> bookmarkedMyChatroomIds = bookmarks.stream().map(Bookmarks::getMyChatroomId)
				.collect(Collectors.toSet());
		Set<Integer> bookmarkedMessageIds = bookmarks.stream().map(Bookmarks::getMessageId).collect(Collectors.toSet());
		MyChatroom mychat = myChatroomService.findMychatByUserID(user.getUserId());
		model.addAttribute("user", user);
		model.addAttribute("workspaceId", workspaceId);
		model.addAttribute("folders", folders);
		model.addAttribute("chatroomId", chatroomId);
		model.addAttribute("tasks", tasks);
		model.addAttribute("chatrooms", chatrooms);
		model.addAttribute("notifications", notifications);
		model.addAttribute("bookmarks", bookmarks);
		model.addAttribute("bookmarkedChatroomIds", bookmarkedChatroomIds);
		model.addAttribute("bookmarkedMyChatroomIds", bookmarkedMyChatroomIds);
		model.addAttribute("bookmarkedMessageIds", bookmarkedMessageIds);
		model.addAttribute("messages", messages);
		model.addAttribute("mychat", mychat);
		return "workspace/workspace_sidebar_modifychat";
	}

	// 🌿 사이드바 메시지 답글 GET
	@GetMapping("/workspace/{workspaceId}/{chatroomId}/side/message/{messageId}")
	public String workspaceMain_SidebarMessage(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId, @PathVariable("messageId") int messageId, Model model,
			Principal principal) {
		List<Folders> folders = workspaceService.getFolderWithChatrooms(workspaceId);
		List<Tasks> tasks = workspaceService.getTask(chatroomId);
		List<Notifications> notifications = workspaceService.getNotifi(chatroomId);
		List<Messages> messages = messageService.getMessage(chatroomId);
		Chatrooms chatrooms = this.workspaceService.getChatroomWithChatId(chatroomId);
		Messages searchMessage = messageService.getMessageWithMessageId(messageId);
		List<Messages> replies = messageService.getReplies(messageId);
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		List<Bookmarks> bookmarks = bookmarkService.getBookmarksByUserId(user.getUserId());
		Set<Integer> bookmarkedChatroomIds = bookmarks.stream().map(Bookmarks::getChatroomId)
				.collect(Collectors.toSet());
		Set<Integer> bookmarkedMyChatroomIds = bookmarks.stream().map(Bookmarks::getMyChatroomId)
				.collect(Collectors.toSet());
		Set<Integer> bookmarkedMessageIds = bookmarks.stream().map(Bookmarks::getMessageId).collect(Collectors.toSet());
		MyChatroom mychat = myChatroomService.findMychatByUserID(user.getUserId());

		model.addAttribute("user", user);
		model.addAttribute("workspaceId", workspaceId);
		model.addAttribute("folders", folders);
		model.addAttribute("chatroomId", chatroomId);
		model.addAttribute("tasks", tasks);
		model.addAttribute("chatrooms", chatrooms);
		model.addAttribute("notifications", notifications);
		model.addAttribute("messages", messages);
		model.addAttribute("bookmarks", bookmarks);
		model.addAttribute("bookmarkedChatroomIds", bookmarkedChatroomIds);
		model.addAttribute("bookmarkedMyChatroomIds", bookmarkedMyChatroomIds);
		model.addAttribute("bookmarkedMessageIds", bookmarkedMessageIds);
		model.addAttribute("searchMessage", searchMessage);
		model.addAttribute("replies", replies);
		model.addAttribute("mychat", mychat);
		return "workspace/workspace_sidebar_message";
	}

	// 🌿 채팅방 정보 수정 POST
	@PostMapping("/workspace/{workspaceId}/{chatroomId}/chatroomModify")
	public String workspaceMain_SidebarChat_Modifiy(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId, Model model, Principal principal,
			@RequestParam("chatroom_discription") String chatroomName, @RequestParam("chatName") String discription) {
		chatroomService.updateChatroom(chatroomId, discription, chatroomName);
		return String.format("redirect:/workspace/" + workspaceId + "/" + chatroomId);
	}

	// 🌿 할 일 생성 POST
	@PostMapping("workspace/{workspaceId}/{chatroomId}/taskcreate")
	public String taskcreate(@PathVariable("workspaceId") int workspaceId, @PathVariable("chatroomId") int chatroomId,
			@RequestParam("taskTitle") String content, @RequestParam("description") String description,
			Principal principal) {

		Optional<Users> _user = userService.getUser(principal.getName());
		Users user = _user.get();

		LocalDateTime startDate = LocalDateTime.of(2025, 5, 16, 9, 46);
		LocalDateTime endDate = LocalDateTime.of(2025, 5, 17, 19, 46);

		TaskDto dto = new TaskDto();
		dto.setAuthorId(user.getUserId());
		dto.setChatroomId(chatroomId);
		dto.setTaskTitle(content);
		dto.setDescription(description);
		dto.setStartDate(startDate);
		dto.setEndDate(endDate);
		dto.setWorkspaceId(workspaceId);

		try {
			taskService.create(dto);
			return String.format("redirect:/workspace/%d/%d", workspaceId, chatroomId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// 🌿 할 일 진행률 업데이트 POST
	@PostMapping("/workspace/{workspaceId}/{chatroomId}/side/{taskId}/modify")
	public String workspaceMain_SidebarTask_Modifiy(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId, @PathVariable("taskId") int taskId,
			@RequestParam(value = "status") int status, Model model, Principal principal) {
		System.out.println("진도율 수정 요청");
		Tasks tasksDetail = this.workspaceService.getTaskWithTaskId(taskId);
		this.taskService.modifyStatus(tasksDetail, status);
		return String.format("redirect:/workspace/" + workspaceId + "/" + chatroomId + "/side/" + taskId);
	}

	// 🌿 공지사항 등록 POST
	@PostMapping("workspace/{workspaceId}/{chatroomId}/notificationcreate")
	public String notificationcreate(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId, @RequestParam(value = "notification_content") String content,
			Principal principal) {

		Optional<Users> _user = userService.getUser(principal.getName());
		Users user = _user.get();

		NotificationDTO dto = new NotificationDTO();
		dto.setAuthorId(user.getUserId());
		dto.setChatroomId(chatroomId);
		dto.setWorkspaceId(workspaceId);
		dto.setContent(content);

		try {
			notificationService.createOrUpdate(dto);
			return String.format("redirect:/workspace/%d/%d", workspaceId, chatroomId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	// 🌿 공지사항 삭제 POST
	@PostMapping("/workspace/{workspaceId}/{chatroomId}/notification/delete")
	public String deleteNotification(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId) {
		notificationService.deleteByChatroomId(chatroomId);
		return "redirect:/workspace/" + workspaceId + "/" + chatroomId;
	}

	// 🌿 즐겨찾기 등록 POST
	@PostMapping("/workspace/bookmark/toggle")
	@ResponseBody
	public Map<String, Object> bookmarkCreate(@RequestBody Map<String, String> payload, Principal principal) {
		Map<String, Object> result = new HashMap<>();
		try {
			String type = payload.get("type");
			int targetId = Integer.parseInt(payload.get("targetId"));
			String action = payload.get("action");

			Optional<Users> _user = this.userService.getUser(principal.getName());
			Users user = _user.get();

			if ("ADD".equals(action)) {
				this.bookmarkService.createBookmark(user, type, targetId);
				System.out.println("즐겨찾기 추가 >> user : " + user.getUserName() + ", type : " + type);
			} else {
				this.bookmarkService.deleteBookmark(user, type, targetId);
				System.out.println("즐겨찾기 삭제 >> user : " + user.getUserName() + ", type : " + type);
			}
			result.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			result.put("success", false);
		}

		return result;
	}

	// 🌿 조직도 GET
	@GetMapping("workspace/organization")
	public String getOrganizationChar(Principal principal, Model model) {
		List<Users> members = this.memberService.findUsersByWorkspaceId(1);
		model.addAttribute("members", members);

		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();

		model.addAttribute("user", user);

		System.out.println("member.size() : " + members.size());
		return "workspace/organization_chart";
	}

	// 🌿 알람 사이드바 GET
	@GetMapping("/{workspaceId}/alarm")
	public String getAlarm(@PathVariable("workspaceId") int workspaceId, Principal principal, Model model) {
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();

		model.addAttribute("user", user);

		List<Alarms> alarmList = alarmService.getAlarmsByWorkspaceId(user.getUserId(), workspaceId);
		model.addAttribute("alarmList", alarmList);

		return "workspace/side_alarm";
	}

}
