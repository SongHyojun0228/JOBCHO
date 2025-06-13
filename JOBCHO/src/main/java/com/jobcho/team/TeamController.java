package com.jobcho.team;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.jobcho.workspace.WorkspaceService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/team")
public class TeamController {
	private final WorkspaceService workspaceService;

	@Value("${file.upload-dir}")
	private String uploadDir;

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private Long getCurrentUserId() {
		String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		try {
			return jdbcTemplate.queryForObject(
				"SELECT USER_ID FROM USERS WHERE USER_EMAIL = ?", Long.class, userEmail);
		} catch (Exception e) {
			return -1L;
		}
	}
	
	private boolean isOwner(Long workspaceId) {
		Long ownerId = jdbcTemplate.queryForObject(
			"SELECT WORKSPACE_OWNER_ID FROM WORKSPACES WHERE WORKSPACE_ID = ?", Long.class, workspaceId);
		Long currentUserId = getCurrentUserId();
		return currentUserId > 0 && currentUserId.equals(ownerId);
	}

	@GetMapping
	public String teamCreate() {
		return "team/team_create";
	}

	@GetMapping("/{workspaceId}/manage")
	public String teamManage(@PathVariable("workspaceId") Long workspaceId, Model model) {
		if (!isOwner(workspaceId)) {
			return "redirect:/index?accessDenied=true";
		}
		
		String fileName = jdbcTemplate.queryForObject(
				"SELECT NVL(WORKSPACE_IMG, WORKSPACE_TEMP_IMG) FROM WORKSPACES WHERE WORKSPACE_ID = ?", String.class, workspaceId);
		String teamName = jdbcTemplate.queryForObject(
				"SELECT WORKSPACE_NAME FROM WORKSPACES WHERE WORKSPACE_ID = ?",String.class, workspaceId);
		
		model.addAttribute("teamName", teamName);
		model.addAttribute("uploadedImage", "/upload/" + fileName);
		model.addAttribute("workspaceId", workspaceId);
		return "team/team_manage";
	}

	@PostMapping("/{workspaceId}/imageupload")
	public String uploadImage(@PathVariable("workspaceId") Long workspaceId, @RequestParam("file") MultipartFile multipartFile, Model model) throws IOException {
		if (!isOwner(workspaceId)) {
			return "redirect:/index?accessDenied=true";
		}
		
		Path dir = Paths.get(uploadDir);
		Files.createDirectories(dir);
		String original = multipartFile.getOriginalFilename();
		String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.') + 1) : "";
		String storedName = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);
		Files.copy(multipartFile.getInputStream(), dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);

		if (!Files.probeContentType(dir.resolve(storedName)).startsWith("image")) {
			Files.deleteIfExists(dir.resolve(storedName));
			return "redirect:/team/" + workspaceId + "/manage?error=notimage";
		}

		jdbcTemplate.update("UPDATE WORKSPACES SET WORKSPACE_TEMP_IMG = ? WHERE WORKSPACE_ID = ?", storedName, workspaceId);
		model.addAttribute("uploadedImage", "/upload/" + storedName);
		model.addAttribute("workspaceId", workspaceId);
		return "team/image_modify";
	}

	@PostMapping("/{workspaceId}/crop")
	public ResponseEntity<?> cropImage(@PathVariable("workspaceId") long workspaceId, @RequestParam("croppedImage") MultipartFile croppedImage) throws IOException {
		if (!isOwner(workspaceId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");
		}

		
		Path dir = Paths.get(uploadDir);
		Files.createDirectories(dir);

		String storedName = UUID.randomUUID().toString() + ".png";
		Path dest = dir.resolve(storedName);
		Files.copy(croppedImage.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

		jdbcTemplate.update("UPDATE WORKSPACES SET WORKSPACE_IMG = ?, WORKSPACE_TEMP_IMG = NULL WHERE WORKSPACE_ID = ?",
				storedName, workspaceId);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/{workspaceId}/temp-image")
	public ResponseEntity<Resource> serveTempImage(@PathVariable("workspaceId") Long workspaceId) throws IOException {
		if (!isOwner(workspaceId)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		String fileName = jdbcTemplate.queryForObject(
			"SELECT NVL(WORKSPACE_TEMP_IMG, WORKSPACE_IMG) FROM WORKSPACES WHERE WORKSPACE_ID = ?", String.class, workspaceId);

		if (fileName == null) {
			return ResponseEntity.notFound().build();
		}

		Path file = Paths.get(uploadDir).resolve(fileName);
		if (!Files.exists(file)) {
			return ResponseEntity.notFound().build();
		}

		Resource resource = new UrlResource(file.toUri());
		String contentType = Files.probeContentType(file);
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
	}


	@PostMapping("/{workspaceId}/delete-icon")
	public String deleteIcon(@PathVariable("workspaceId") Long workspaceId) throws IOException {
		if (!isOwner(workspaceId)) {
			return "redirect:/index?accessDenied=true";
		}
		
		Path dir = Paths.get(uploadDir);
		String old = jdbcTemplate.queryForObject("SELECT WORKSPACE_IMG FROM WORKSPACES WHERE WORKSPACE_ID = ?", String.class, workspaceId);
		if (old != null)
			Files.deleteIfExists(dir.resolve(old));
		jdbcTemplate
				.update("UPDATE WORKSPACES SET WORKSPACE_IMG = NULL, WORKSPACE_TEMP_IMG = NULL WHERE WORKSPACE_ID = ?", workspaceId);
		return "redirect:/team/" + workspaceId + "/manage";
	}

	@PostMapping("/{workspaceId}/change-name")
	public String changeName(@PathVariable("workspaceId") Long workspaceId, @RequestParam("teamName") String newName) {
		if (!isOwner(workspaceId)) {
			return "redirect:/index?accessDenied=true";
		}
		
		jdbcTemplate.update("UPDATE WORKSPACES SET WORKSPACE_NAME = ? WHERE WORKSPACE_ID = ?", newName, workspaceId);
		return "redirect:/team/" + workspaceId + "/manage";
	}

	@PostMapping("/{workspaceId}/delete-team")
	public String deleteTeam(@PathVariable("workspaceId") Long workspaceId) {
		if (!isOwner(workspaceId)) {
			return "redirect:/index?accessDenied=true";
		}
		
		jdbcTemplate.update("DELETE FROM WORKSPACES WHERE WORKSPACE_ID = ?", workspaceId);
		return "redirect:/";
	}
	
	@GetMapping("/{workspaceId}/icon")
	@ResponseBody
	public ResponseEntity<Resource> getTeamIcon(@PathVariable("workspaceId") Integer workspaceId) throws IOException {
		String fileName = jdbcTemplate.queryForObject(
			"SELECT NVL(WORKSPACE_IMG, WORKSPACE_TEMP_IMG) FROM WORKSPACES WHERE WORKSPACE_ID = ?",
			String.class, workspaceId);

		if (fileName == null || fileName.trim().isEmpty()) {
			ClassPathResource defaultImg = new ClassPathResource("static/images/profileImg/default_team.png");
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(defaultImg);
		}

		Path imagePath = Paths.get(uploadDir, fileName);	
		if (!Files.exists(imagePath)) {
			ClassPathResource defaultImg = new ClassPathResource("static/images/profileImg/default_team.png");
			return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(defaultImg);
		}

		Resource image = new UrlResource(imagePath.toUri());
		String contentType = Files.probeContentType(imagePath);
		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(contentType != null ? contentType : "application/octet-stream"))
			.body(image);
	}


}
