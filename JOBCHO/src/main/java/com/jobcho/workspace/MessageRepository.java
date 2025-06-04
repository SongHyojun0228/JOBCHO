package com.jobcho.workspace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Messages, Integer>{
	List<Messages> findByChatroomId(int chatroomId);
}
