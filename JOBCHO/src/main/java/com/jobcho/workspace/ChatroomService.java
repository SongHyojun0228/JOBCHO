package com.jobcho.workspace;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatroomService {

	private final ChatroomRepository chatroomRepository;
	
	public void create(int folderid, int createdby, String chatroomname, String description, int isPrivate) {
		Chatrooms c = new Chatrooms();
		c.setFolderId(folderid);
		c.setCreatedBy(createdby);
		c.setChatroomName(chatroomname);
		c.setDescription(description);
		c.setIsPrivate(isPrivate);
		this.chatroomRepository.save(c);
	}
	
}
