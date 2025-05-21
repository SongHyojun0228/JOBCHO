package com.jobcho.workspace;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class NotificationService {
	
	private final NotificationRepository notificationRepository;
	
	public void create(int chatroomid, int authorId, String content) {
		Notifications n = new Notifications();
		n.setAuthorId(authorId);
		n.setChatroomId(chatroomid);
		n.setContent(content);
		n.setTitle("null");
		n.setIsEdited(0);
		n.setIsDeleted(0);
		this.notificationRepository.save(n);
	}

}
