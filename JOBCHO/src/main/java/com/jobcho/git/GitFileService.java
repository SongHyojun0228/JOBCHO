package com.jobcho.git;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GitFileService {

	private final GitFileRepository gitFileRepository;

	void uploadGitFiles(GitFolder gitFolder, Commit commit, String fileName) {

		GitFile gitFile = new GitFile();
		
		gitFile.setGitFolder(gitFolder);
		gitFile.setCommit(commit);
		gitFile.setFileName(fileName);
		
		try {			
			this.gitFileRepository.save(gitFile);
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("DB에 깃파일 올리는 도중 실패");
		}
	}
	
	List<GitFile> getAllGitFiles() {
		return this.gitFileRepository.findAll();
	}

}
