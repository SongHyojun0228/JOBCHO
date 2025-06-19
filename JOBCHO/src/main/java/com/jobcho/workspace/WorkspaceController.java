package com.jobcho.workspace;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jobcho.alarm.AlarmRepository;
import com.jobcho.alarm.AlarmService;
import com.jobcho.alarm.Alarms;
import com.jobcho.bookmark.BookmarkService;
import com.jobcho.bookmark.Bookmarks;
import com.jobcho.chatroom.ChatroomService;
import com.jobcho.chatroom.Chatrooms;
import com.jobcho.chatroom_member.ChatroomMemberService;
import com.jobcho.file.FileService;
import com.jobcho.folder.FolderService;
import com.jobcho.folder.Folders;
import com.jobcho.member.MemberService;
import com.jobcho.mention.MentionService;
import com.jobcho.mention.Mentions;
import com.jobcho.message.MessageDto;
import com.jobcho.message.MessageService;
import com.jobcho.message.Messages;
import com.jobcho.mychatroom.MyChatroom;
import com.jobcho.mychatroom.MyChatroomService;
import com.jobcho.notification.NotificationService;
import com.jobcho.notification.Notifications;
import com.jobcho.task.TaskService;
import com.jobcho.task.Tasks;
import com.jobcho.user.UserService;
import com.jobcho.user.Users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor // final이 붙은 속성을 포함하는 생성자를 자동으로 만들어줌
@Controller
public class WorkspaceController {

	private final FolderService folderService;
	private final NotificationService notificationService;
	private final BookmarkService bookmarkService;
	private final WorkspaceService workspaceService;
	private final TaskService taskService;
	private final UserService userService;
	private final MemberService memberService;
	private final ChatroomMemberService chatroomMemberService;
	private final MessageService messageService;
	private final AlarmRepository alarmRepository;
	private final AlarmService alarmService;
	private final MyChatroomService myChatroomService;
	private final ChatroomService chatroomService;
	private final MentionService mentionService;
	private final FileService fileService;

	// 🌿 워크 스페이스 생성 GET
	@GetMapping("/workspace/create")
	public String getCreateWorkspcae(WorkspaceCreateForm workspaceCreateForm, Principal principal, Model model) {
		if (principal == null) {
			return "user/login";
		}

		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		model.addAttribute("user", user);

		System.out.println("getCreateWorkspcae(), userId : " + user.getUserId());

		model.addAttribute("workspaceCreateForm", new WorkspaceCreateForm());

		return "workspace/create_workspace";
	}

	// 🌿 워크 스페이스 생성 POST
	@PostMapping("/workspace/create")
	public String createWorkspace(@Valid WorkspaceCreateForm workspaceCreateForm, BindingResult bindingResult,
			Principal principal) {
		if (bindingResult.hasErrors()) {
			return "workspace/create_workspace";
		}

		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();

		try {
			Integer workspaceId = this.workspaceService.createWorkspace(user, workspaceCreateForm.getWorkspaceName(),
					workspaceCreateForm.getWorkspaceDomain() + ".jobcho.com");
			return "redirect:/workspace/" + workspaceId;
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			bindingResult.reject("createWorkspaceFailed", "이미 등록된 도메인 주소입니다.");
			return "workspace/create_workspace";
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("createWorkspaceFailed", e.getMessage());
			return "workspace/create_workspace";
		}
	}

	// 🌿 워크스페이스 매핑
	@GetMapping("/workspace/{workspaceId}")
	public String workspaceMain(@PathVariable("workspaceId") int workspaceId, Model model, Principal principal) {
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		List<Users> members = this.memberService.findUsersByWorkspaceId(workspaceId);

		List<Folders> folders = workspaceService.getFolderWithChatrooms(workspaceId);
		List<Bookmarks> bookmarks = bookmarkService.getBookmarksByUserId(user.getUserId());
		MyChatroom mychat = myChatroomService.findMychatByUserID(user.getUserId());

		Set<Integer> bookmarkedChatroomIds = bookmarkService.extractChatroomBookmarkIds(bookmarks);
		Set<Integer> bookmarkedMyChatroomIds = bookmarkService.extractMyChatroomBookmarkIds(bookmarks);
		Set<Integer> bookmarkedMessageIds = bookmarkService.extractMessageBookmarkIds(bookmarks);

		model.addAttribute("user", user);
		model.addAttribute("members", members);
		model.addAttribute("workspaceId", workspaceId);
		model.addAttribute("folders", folders);
		model.addAttribute("bookmarks", bookmarks);
		model.addAttribute("bookmarkedChatroomIds", bookmarkedChatroomIds);
		model.addAttribute("bookmarkedMyChatroomIds", bookmarkedMyChatroomIds);
		model.addAttribute("bookmarkedMessageIds", bookmarkedMessageIds);
		model.addAttribute("mychat", mychat);

		return "workspace/workspace_basic";
	}

	// 🌿 나와의 채팅 GET
	@GetMapping("/workspace/{workspaceId}/mychat/{mychatroomId}")
	public String workspaceMain_mychat(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("mychatroomId") int mychatroomId, Model model, Principal principal) {
		List<Folders> folders = workspaceService.getFolderWithChatrooms(workspaceId);
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		List<Users> members = this.memberService.findUsersByWorkspaceId(workspaceId);
		List<Bookmarks> bookmarks = bookmarkService.getBookmarksByUserId(user.getUserId());
		Set<Integer> bookmarkedChatroomIds = bookmarkService.extractChatroomBookmarkIds(bookmarks);
		Set<Integer> bookmarkedMyChatroomIds = bookmarkService.extractMyChatroomBookmarkIds(bookmarks);
		Set<Integer> bookmarkedMessageIds = bookmarkService.extractMessageBookmarkIds(bookmarks);

		model.addAttribute("user", user);
		model.addAttribute("members", members);
		model.addAttribute("workspaceId", workspaceId);
		model.addAttribute("MychatroomId", mychatroomId);
		model.addAttribute("folders", folders);
		model.addAttribute("bookmarks", bookmarks);
		model.addAttribute("bookmarkedChatroomIds", bookmarkedChatroomIds);
		model.addAttribute("bookmarkedMyChatroomIds", bookmarkedMyChatroomIds);
		model.addAttribute("bookmarkedMessageIds", bookmarkedMessageIds);
		return "workspace/workspace_mychat";
	}

	// 🌿 워크 스페이스 내 채팅방 매핑
	@GetMapping("/workspace/{workspaceId}/{chatroomId}")
	public String workspaceMain_selectChatRoom(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId, Model model, Principal principal) {
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		List<Users> members = this.memberService.findUsersByWorkspaceId(workspaceId);
		List<Users> chatroomMembers = this.chatroomMemberService.findUsersByChatroomId(chatroomId);

		List<Folders> folders = workspaceService.getFolderWithChatrooms(workspaceId);
		List<Tasks> tasks = workspaceService.getTask(chatroomId);
		List<Notifications> notifications = workspaceService.getNotifi(chatroomId);
//		List<Messages> messages = messageService.getMessage(chatroomId);
		List<Messages> messages = messageService.getTopLevelMessagesWithReplies(chatroomId);
		List<Bookmarks> bookmarks = bookmarkService.getBookmarksByUserId(user.getUserId());
		Chatrooms chatrooms = this.workspaceService.getChatroomWithChatId(chatroomId);
		List<Alarms> alarms = this.alarmRepository.findByUserIdAndIsNotRead(user.getUserId());
		MyChatroom mychat = myChatroomService.findMychatByUserID(user.getUserId());
		Set<Integer> bookmarkedChatroomIds = bookmarkService.extractChatroomBookmarkIds(bookmarks);
		Set<Integer> bookmarkedMyChatroomIds = bookmarkService.extractMyChatroomBookmarkIds(bookmarks);
		Set<Integer> bookmarkedMessageIds = bookmarkService.extractMessageBookmarkIds(bookmarks);
		List<Mentions> mentions = mentionService.getByChatroomId(chatroomId);

		model.addAttribute("user", user);
		model.addAttribute("members", members);
		model.addAttribute("chatroomMembers", chatroomMembers);
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
		model.addAttribute("alarms", alarms);
		model.addAttribute("mychat", mychat);
		model.addAttribute("mentions", mentions);

		return "workspace/workspace";
	}

	// 🌿 폴더 생성 POST
	@PostMapping("workspace/{workspaceId}/foldercreate")
	public String folderCreate(@PathVariable("workspaceId") int workspaceId,
			@RequestParam(value = "folder_name") String folder_name) {
		this.folderService.create(workspaceId, folder_name, 1); // 워크스페이스,이름,생성자_id
		return String.format("redirect:/workspace/" + workspaceId);
	}

	// 🌿 채팅방 생성 POST
	@PostMapping("workspace/{workspaceId}/chatroomcreate")
	public String chatroomcreate(@PathVariable("workspaceId") int workspaceId,
			@RequestParam(value = "folderId") int folderId, @RequestParam(value = "createduserId") int createduserId,
			@RequestParam(value = "chatName") String chatname, @RequestParam(value = "isPrivate") int isPrivate,
			@RequestParam(value = "chatroom_discription") String discription) {
		this.chatroomService.create(folderId, createduserId, chatname, discription, isPrivate);
		return String.format("redirect:/workspace/" + workspaceId);
	}

	// 🌿 채팅 메세지 삭제 POST
	@PostMapping("/workspace/{workspaceId}/{chatroomId}/message/{messageId}/delete")
	public String deleteMessage(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId, @PathVariable("messageId") int messageId) {
		messageService.deleteMessage(messageId);
		return "redirect:/workspace/" + workspaceId + "/" + chatroomId;
	}

	// 🌿 채팅 메세지 수정 POST
	@PostMapping("/workspace/{workspaceId}/{chatroomId}/messageUpdate")
	public String updateMessage(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId, @RequestParam("messageId") int messageId,
			@RequestParam("message_content") String newContent) {

		messageService.updateMessage(messageId, newContent);
		return "redirect:/workspace/" + workspaceId + "/" + chatroomId;
	}

	// 🌿 채팅 메세지 답글 등록 POST
	@PostMapping("/workspace/{workspaceId}/{chatroomId}/chatAnswerAdd/{messageId}")
	public String createMessageAnswer(@PathVariable("workspaceId") int workspaceId,
			@PathVariable("chatroomId") int chatroomId, @PathVariable("messageId") int messageId,
			@RequestParam("answer_input") String answerContent, Principal principal) {
		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();
		int senderId = user.getUserId();
		messageService.addReply(chatroomId, messageId, answerContent, senderId);
		System.out.println("답글 호출됨: " + answerContent);
		return "redirect:/workspace/" + workspaceId + "/" + chatroomId + "/side/message/" + messageId;
	}

	// 🌿 채팅 파일 등록 POST
	@PostMapping("/workspace/{workspaceId}/{chatroomId}/message/upload")
	@ResponseBody
	public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file,
			@RequestParam("messageId") Integer messageId, @PathVariable("chatroomId") Integer chatroomId,
			@PathVariable("workspaceId") Integer workspaceId, Principal principal) throws IOException {

		Optional<Users> _user = userService.getUser(principal.getName());
		if (_user.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
		}

		Users user = _user.get();
		Integer senderId = user.getUserId();

		fileService.uploadFile(file, messageId, senderId, chatroomId);
		return ResponseEntity.ok().body("파일 업로드 성공");
	}

}
