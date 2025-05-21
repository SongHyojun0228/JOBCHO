package com.jobcho.workspace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notifications, Integer>{
	List<Notifications> findByChatroomId(int chatroomId);
}
