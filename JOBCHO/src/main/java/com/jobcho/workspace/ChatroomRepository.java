package com.jobcho.workspace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomRepository extends JpaRepository<Chatrooms, Integer>{
	List<Chatrooms> findByFolderId(int folderId);
}
