package com.jobcho.team;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/team")
public class TeamController {

	@Value("${file.upload-dir}")
	private String uploadDir;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@GetMapping
	public String teamCreate() {
		return "team/team_create";
	}

	@GetMapping("/manage")
	public String teamManage(Model model) {
		String fileName = jdbcTemplate.queryForObject(
				"SELECT NVL(WORKSPACE_TEMP_IMG, WORKSPACE_IMG) FROM WORKSPACES WHERE WORKSPACE_ID = ?", String.class,
				1L);
		String teamName = jdbcTemplate.queryForObject("SELECT WORKSPACE_NAME FROM WORKSPACES WHERE WORKSPACE_ID = ?",
				String.class, 1L);
		model.addAttribute("teamName", teamName);
		model.addAttribute("uploadedImage", fileName);
		model.addAttribute("workspaceId", 1L);
		return "team/team_manage";
	}

	@PostMapping("/imageupload")
	public String uploadImage(@RequestParam("file") MultipartFile multipartFile, Model model) throws IOException {
		Path dir = Paths.get(uploadDir);
		Files.createDirectories(dir);
		String original = multipartFile.getOriginalFilename();
		String ext = (original != null && original.contains(".")) ? original.substring(original.lastIndexOf('.') + 1)
				: "";
		String storedName = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);
		Files.copy(multipartFile.getInputStream(), dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);

		if (!Files.probeContentType(dir.resolve(storedName)).startsWith("image")) {
			Files.deleteIfExists(dir.resolve(storedName));
			return "redirect:/team/manage?error=notimage";
		}

		jdbcTemplate.update("UPDATE WORKSPACES SET WORKSPACE_TEMP_IMG = ? WHERE WORKSPACE_ID = 1", storedName);
		model.addAttribute("uploadedImage", "/upload/" + storedName);
		return "team/image_modify";
	}

	@PostMapping("/crop")
	public ResponseEntity<?> cropImage(@RequestParam("croppedImage") MultipartFile croppedImage) throws IOException {
		Path dir = Paths.get(uploadDir);
		Files.createDirectories(dir);

		String storedName = UUID.randomUUID().toString() + ".png";
		Path dest = dir.resolve(storedName);
		Files.copy(croppedImage.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

		jdbcTemplate.update("UPDATE WORKSPACES SET WORKSPACE_IMG = ?, WORKSPACE_TEMP_IMG = NULL WHERE WORKSPACE_ID = 1",
				storedName);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/temp-image/{id}")
	public ResponseEntity<Resource> serveTempImage(@PathVariable("id") Long id) throws IOException {
		String sql = "SELECT NVL(WORKSPACE_TEMP_IMG, WORKSPACE_IMG) FROM WORKSPACES WHERE WORKSPACE_ID = ?";
		String fileName = jdbcTemplate.queryForObject(sql, String.class, id);

		Path file = Paths.get(uploadDir).resolve(fileName);
		Resource resource = new UrlResource(file.toUri());

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(Files.probeContentType(file))).body(resource);
	}

	@PostMapping("/delete-icon")
	public String deleteIcon() throws IOException {
		Path dir = Paths.get(uploadDir);
		String old = jdbcTemplate.queryForObject("SELECT WORKSPACE_IMG FROM WORKSPACES WHERE WORKSPACE_ID = 1",
				String.class);
		if (old != null)
			Files.deleteIfExists(dir.resolve(old));
		jdbcTemplate
				.update("UPDATE WORKSPACES SET WORKSPACE_IMG = NULL, WORKSPACE_TEMP_IMG = NULL WHERE WORKSPACE_ID = 1");
		return "redirect:/team/manage";
	}

	@PostMapping("/change-name")
	public String changeName(@RequestParam("teamName") String newName) {
		jdbcTemplate.update("UPDATE WORKSPACES SET WORKSPACE_NAME = ? WHERE WORKSPACE_ID = 1", newName);
		return "redirect:/team/manage";
	}

	@PostMapping("/delete-team")
	public String deleteTeam() {
		jdbcTemplate.update("DELETE FROM WORKSPACES WHERE WORKSPACE_ID = 1");
		return "redirect:/";
	}

}
