package com.jobcho.workspace;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.jobcho.member.MemberService;
import com.jobcho.user.Users;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class WorkspaceService {

	private final FolderRepository folderRepository;
	private final ChatroomRepository chatroomRepository;
	private final TaskRepository taskRepository;
	private final NotificationRepository notificationRepository;
	
	private final WorkspaceRepository workspaceRepository;
	private final MemberService memberService;
	
	public List<Folders> getFolderWithChatrooms(int workspaceId){
		List<Folders> folders = folderRepository.findByWorkspaceId(workspaceId);
		
		for(Folders folder : folders) {
			System.out.println(folder.getFolderId());
			List<Chatrooms> chatrooms = chatroomRepository.findByFolderId(folder.getFolderId());
			folder.setChatrooms(chatrooms);
		}
		
		return folders;
	}
	
	public List<Tasks> getTask(int chatroomId){
		List<Tasks> tasks = taskRepository.findByChatroomId(chatroomId);
		return tasks;
	}
	
	public List<Notifications> getNotifi(int chatroomId){
		List<Notifications> notifications = notificationRepository.findByChatroomId(chatroomId);
		return notifications;
	}
	
	public Chatrooms getChatroomWithChatId(int chatroomId){
		Optional<Chatrooms> chatroom = this.chatroomRepository.findById(chatroomId);
		if (chatroom.isPresent()) {
			return chatroom.get();
		}else {
			return chatroom.orElse(new Chatrooms());
		}
	}

	public void createWorkspace(Users user, String workspaceName, String workspaceDomain) {
		Workspaces workspace = new Workspaces();
		workspace.setWorkspaceName(workspaceName);
		workspace.setWorkspaceDomain(workspaceDomain);
		workspace.setOwner(user);

		Workspaces savedWorkspace = workspaceRepository.save(workspace);

		memberService.createMember(user, savedWorkspace);

		System.out.println("workspaceRepository : " + workspaceRepository);
	}

	public String getWorkspaceNameById(Integer workspaceId) {
		Workspaces workspcace = this.workspaceRepository.getById(workspaceId);
		return workspcace.getWorkspaceName();
	}
	
	public Workspaces findById(Integer id) {
		return workspaceRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("해당 워크스페이스가 없습니다."));
	}

}
