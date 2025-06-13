package com.jobcho.cs;

import org.springframework.stereotype.Service;

import com.jobcho.user.Users;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CsChatroomService {

	private final CsChatroomRepository csChatroomRepository;
	
    // 🌿 1 : 1 고객센터 채팅방 생성 메서드 
	public void createCsChatRoom(Users user) {
		CsChatroom csChatroom = new CsChatroom();
		csChatroom.setUser(user);
		
		this.csChatroomRepository.save(csChatroom);
	}
	
}
