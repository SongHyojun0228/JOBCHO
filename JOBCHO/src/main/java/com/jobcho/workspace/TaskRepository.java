package com.jobcho.workspace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Tasks, Integer>{
	List<Tasks> findByChatroomId(int chatroomId);
}
