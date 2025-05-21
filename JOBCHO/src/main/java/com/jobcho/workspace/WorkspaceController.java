package com.jobcho.workspace;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jobcho.member.MemberService;
import com.jobcho.user.UserService;
import com.jobcho.user.Users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor //final이 붙은 속성을 포함하는 생성자를 자동으로 만들어줌
@Controller
public class WorkspaceController {

    private final FolderService folderService;
	private final FolderRepository folderRepository;
	private final NotificationService notificationService;
	private final BookmarkService boookmarkService;
	private final WorkspaceService workspaceService;
	private final TaskService taskService;
	private final UserService userService;
	private final MemberService memberService;
	
	@GetMapping("/workspace/test")
	public String list(Model model) {
		return "workspace/chat_test";
		}
	
	@GetMapping("/workspace/{workspaceId}")
	public String workspaceMain(@PathVariable("workspaceId") int workspaceId, Model model) {
		List<Folders> folders = workspaceService.getFolderWithChatrooms(workspaceId);
		model.addAttribute("workspaceId", workspaceId);
		model.addAttribute("folders",folders);
		return "workspace/workspace_nomal";
	}
	
	@GetMapping("/workspace/{workspaceId}/{chatroomId}")
	public String workspaceMain_selectChatRoom(@PathVariable("workspaceId") int workspaceId, @PathVariable("chatroomId") int chatroomId,Model model) {
		List<Folders> folders = workspaceService.getFolderWithChatrooms(workspaceId);
		List<Tasks> tasks = workspaceService.getTask(chatroomId);
		List<Notifications> notifications = workspaceService.getNotifi(chatroomId);
		Chatrooms chatrooms = this.workspaceService.getChatroomWithChatId(chatroomId);
		model.addAttribute("workspaceId", workspaceId);
		model.addAttribute("folders",folders);
		model.addAttribute("chatroomId",chatroomId);
		model.addAttribute("tasks",tasks);
		model.addAttribute("chatrooms",chatrooms);
		model.addAttribute("notifications",notifications);
		return "workspace/workspace";
	}
	
	@PostMapping("workspace/{workspaceId}/{chatroomId}/taskcreate")
	public String taskcreate(@PathVariable("workspaceId") int workspaceId, @PathVariable("chatroomId") int chatroomId, @RequestParam(value="taskTitle") String content, @RequestParam(value="description") String description) {
		LocalDateTime startDate = LocalDateTime.of(2025, 5, 16, 9, 46, 0); // 시작날 임시로, 나중에 셀렉트에서 가져오기 
		LocalDateTime endDate = LocalDateTime.of(2025, 5, 17, 19, 46, 0);
		try {
			this.taskService.create(1,chatroomId,content,description,startDate,endDate); // 작성자id,챗룸id, 제목,내용,시작날,끝날,나만의챗의 고유번호(아니면 null)
			return String.format("redirect:/workspace/"+workspaceId+"/"+chatroomId);
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@PostMapping("workspace/{workspaceId}/foldercreate")
	public String folderCreate(@PathVariable("workspaceId") int workspaceId, @RequestParam(value="folder_name") String folder_name) {
		this.folderService.create(workspaceId, folder_name, 1); //워크스페이스,이름,생성자_id
		return String.format("redirect:/workspace/"+workspaceId);
	}
	
	@PostMapping("workspace/{workspaceId}/{chatroomId}/notificationcreate")
	public String notificationcreate(@PathVariable("workspaceId") int workspaceId, @PathVariable("chatroomId") int chatroomId, @RequestParam(value="notification_content") String content) {
		this.notificationService.create(chatroomId, 1, content); // 챗룸, 이용자, 내용
		return String.format("redirect:/workspace/"+workspaceId+"/"+chatroomId);
	}
	
	@PostMapping("workspace/bookmarkcreate/{origin}")
	public String bookmarkCreate(@PathVariable("origin") String origin) {
		this.boookmarkService.create(3, origin, 2); //유저id, 확인 문자열, 그 컨텐츠 id번호
		return "workspace/workspace";
	}

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

	@PostMapping("/workspace/create")
	public String createWorkspace(@Valid WorkspaceCreateForm workspaceCreateForm, BindingResult bindingResult,
			Principal principal) {
		if (bindingResult.hasErrors()) {
			return "workspace/create_workspace";
		}

		Optional<Users> _user = this.userService.getUser(principal.getName());
		Users user = _user.get();

		try {
			this.workspaceService.createWorkspace(user, workspaceCreateForm.getWorkspaceName(),
					workspaceCreateForm.getWorkspaceDomain());
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			bindingResult.reject("createWorkspaceFailed", "이미 등록된 팀입니다.");
			return "workspace/create_workspace";
		} catch (Exception e) {
			e.printStackTrace();
			bindingResult.reject("createWorkspaceFailed", e.getMessage());
			System.out.println("팀생성 도중 실패");
			return "workspace/create_workspace";
		}

		System.out.println("팀 생성 완료");

		return "redirect:/index";
	}


	@GetMapping("workspace/organization")
	public String getOrganizationChar(Principal principal, Model model) {
		List<Users> members = this.memberService.findUsersByWorkspaceId(1);
		model.addAttribute("members", members);
		
		System.out.println("member.size() : " + members.size());
		return "workspace/organization_chart";
	}
}
