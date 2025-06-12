package com.jobcho.bookmark;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.jobcho.user.Users;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BookmarkService {

	private final BookmarkRepository bookmarkRepository;

    // 🌿 유저아이디로 즐겨찾기 불러오기 메서드
	public List<Bookmarks> getBookmarksByUserId(Integer userId) { 
		return this.bookmarkRepository.findByUser_UserId(userId);
	}

    // 🌿 즐겨찾기 생성 메서드
	public void createBookmark(Users user, String type, int targetId) {
		Bookmarks b = new Bookmarks();
		b.setUser(user);
		switch (type.toUpperCase()) {
		case "MESSAGE":
			b.setMessageId(targetId);
			break;
		case "NOTIFICATION":
			b.setNotificationId(targetId);
			break;
		case "VOTE":
			b.setVoteId(targetId);
			break;
		case "TASK":
			b.setTaskId(targetId);
			break;
		case "CHATROOM":
			b.setChatroomId(targetId);
			break;
		case "FILE":
			b.setFileId(targetId);
			break;
		case "MYCHATROOM":
			b.setMyChatroomId(targetId);
			break;
		}
		bookmarkRepository.save(b);
	}

    // 🌿 즐겨찾기 삭제 메서드
	public void deleteBookmark(Users user, String type, int targetId) {
	    List<Bookmarks> bookmarks = new ArrayList<>();
	    
	    switch (type.toUpperCase()) {
	        case "CHATROOM":
	            bookmarks = bookmarkRepository.findByUserAndChatroomId(user, targetId);
	            break;
	        case "MESSAGE":
	            bookmarks = bookmarkRepository.findByUserAndMessageId(user, targetId);
	            break;
	        case "NOTIFICATION":
	            bookmarks = bookmarkRepository.findByUserAndNotificationId(user, targetId);
	            break;
	        case "VOTE":
	            bookmarks = bookmarkRepository.findByUserAndVoteId(user, targetId);
	            break;
	        case "TASK":
	            bookmarks = bookmarkRepository.findByUserAndTaskId(user, targetId);
	            break;
	        case "FILE":
	            bookmarks = bookmarkRepository.findByUserAndFileId(user, targetId);
	            break;
	        case "MYCHATROOM":
	        	bookmarks = bookmarkRepository.findByUserAndMyChatroomId(user, targetId);
				break;
			}

	    for (Bookmarks b : bookmarks) {
	        bookmarkRepository.delete(b);
	    }
	}

}
