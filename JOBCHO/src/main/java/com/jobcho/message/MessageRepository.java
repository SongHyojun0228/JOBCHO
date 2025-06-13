package com.jobcho.message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Messages, Integer>{
	List<Messages> findByChatroomId(int chatroomId);
	List<Messages> findByParentMessage_MessageIdOrderByCreatedDateAsc(Integer parentMessageId);

}
