package com.jobcho.git;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommitService {

	private final CommitRepository commitRepository;

	Commit uploadCommit(Branch branch, String commitContent) {
		Commit commit = new Commit();
		commit.setContent(commitContent);
		
		try {
			this.commitRepository.save(commit);
			System.out.println("🌿 커밋 업로드 성공");
			return commit;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("🌿 커밋 업로드 실패");
			return null;
		}
	}

}
