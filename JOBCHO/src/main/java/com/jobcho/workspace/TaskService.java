package com.jobcho.workspace;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TaskService {
 
	private final TaskRepository taskRepository;
	
	public void create(int authorid, int chatroomid, String title, String desc, LocalDateTime sdate, LocalDateTime edate) {
		Tasks t = new Tasks();
		t.setAuthorId(authorid);
		t.setChatroomId(chatroomid);
		t.setTaskTitle(title);
		t.setDescription(desc);
		t.setStatus(0);
		t.setStartDate(sdate);
		t.setEndDate(edate);
		this.taskRepository.save(t);
	}
}
