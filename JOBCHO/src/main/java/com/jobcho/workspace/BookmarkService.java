package com.jobcho.workspace;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BookmarkService {
	
	private final BookmarkRepository bookmarkRepository;
	
	public void create(int userId, String origin, int contentNum) {
		Bookmarks b = new Bookmarks();
		
		if(origin.equals("message")) {
			b.setUserId(userId);
			b.setMessageId(contentNum);
			b.setNotificationId(null);
			b.setVoteId(null);
			b.setTaskId(null);
			b.setChatroomId(null);
			b.setFileId(null);
		}
	}
}
