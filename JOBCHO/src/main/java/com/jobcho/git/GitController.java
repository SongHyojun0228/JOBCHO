package com.jobcho.git;

import java.io.File;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.jobcho.user.UserService;
import com.jobcho.user.Users;
import com.jobcho.workspace.WorkspaceService;
import com.jobcho.workspace.Workspaces;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class GitController {

	private final UserService userService;
	private final WorkspaceService workspaceService;
	private final GitFileService gitFileService;
	private final CommitService commitService;

	@Value("${file.git-upload-dir}")
	private String uploadGitFileDir;

	@GetMapping("/workspace/{workspaceId}/github")
	public String getGitCommitPage(@PathVariable("workspaceId") Integer workspaceId, Principal principal, Model model) {

		Optional<Users> _user = this.userService.getUser(principal.getName());
		Workspaces workspace = this.workspaceService.getWorkspaceByWorkspaceId(workspaceId);
		List<GitFile> allGitFiles = this.gitFileService.getAllGitFiles();

		model.addAttribute("user", _user.get());
		model.addAttribute("workspace", workspace);
		model.addAttribute("allGitFiles", allGitFiles);

		return "git/git_commit";
	}

	@PostMapping("/workspace/{workspaceId}/upload/git")
	public String uploadGit(@PathVariable("workspaceId") Integer workspaceId,
			@RequestParam("git_files") MultipartFile[] gitFiles, @RequestParam("commit_content") String commitContent,
			Model model, Principal principal) {
		

		File uploadGitFile = new File(uploadGitFileDir).getAbsoluteFile();
		if (!uploadGitFile.exists())
			uploadGitFile.mkdir();

		for (MultipartFile file : gitFiles) {
			String gitFileName = file.getOriginalFilename();
			File dest = new File(uploadGitFile, gitFileName);

			try {
				file.transferTo(dest);
				Commit commit = this.commitService.uploadCommit(null, commitContent);
				this.gitFileService.uploadGitFiles(null, commit, gitFileName);
			} catch (Exception e) {
				System.out.println("gitFile 업로드 중 실패");
				e.printStackTrace();
			}
		}

		return "redirect:/workspace/" + workspaceId + "/github";
	}

}
