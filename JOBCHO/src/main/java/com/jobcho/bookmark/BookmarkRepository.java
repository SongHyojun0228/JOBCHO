package com.jobcho.bookmark;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobcho.user.Users;

public interface BookmarkRepository extends JpaRepository<Bookmarks, Integer> {
	public List<Bookmarks> findByUser_UserId(Integer userId);

	public List<Bookmarks> findByUserAndChatroomId(Users user, int targetId);

	public List<Bookmarks> findByUserAndMessageId(Users user, int targetId);

	public List<Bookmarks> findByUserAndNotificationId(Users user, int targetId);

	public List<Bookmarks> findByUserAndTaskId(Users user, int targetId);

	public List<Bookmarks> findByUserAndVoteId(Users user, int targetId);

	public List<Bookmarks> findByUserAndFileId(Users user, int targetId);
	
	public List<Bookmarks> findByUserAndMyChatroomId(Users user, int targetId);
}
