package com.jobcho.user;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.jobcho.cs.CsChatroomService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final CsChatroomService cschatRoomService;

	public Users create(String email, String password, String username) {
		Users user = new Users();
		user.setUserName(username);
		user.setUserEmail(email);
		user.setUserPassword(passwordEncoder.encode(password));
		this.userRepository.save(user);

		cschatRoomService.createCsChatRoom(user);
		return user;
	}

	public Optional<Users> getUser(String userEmail) {
		Optional<Users> user = this.userRepository.findByUserEmail(userEmail);

		return user;
	}

	public Optional<Users> getUserByEmail(String email) {
		return userRepository.findByUserEmail(email);
	}

	public void uploadUserProfile(Users user, String profileImgName) {
		user.setUserImg(profileImgName);
		System.out.println(profileImgName + "로 이미지 변경");
		this.userRepository.save(user);
	}
	
	public void changeUserName(Users user, String newUserName) {
		user.setUserName(newUserName);
		this.userRepository.save(user);
	}

	public void changeUserEmail(Users user, String newUserEmail) {
		user.setUserEmail(newUserEmail);
		this.userRepository.save(user);
	}

	public void changePassword(Users user, String newUserPassword) {
		user.setUserPassword(passwordEncoder.encode(newUserPassword));
		this.userRepository.save(user);
	}

	public void deleteUser(Users user) {
		this.userRepository.delete(user);
	}

	public boolean existsByEmail(String email) {
		return userRepository.findByUserEmail(email).isPresent();
	}

	public boolean checkPassword(Users user, String inputPassword) {
		return passwordEncoder.matches(inputPassword, user.getUserPassword());
	}
	
	public Users findById(Integer id) {
		return userRepository.findById(id).orElse(null);
	}

}
