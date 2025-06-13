package com.jobcho.folder;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FolderService {

	private final FolderRepository folderRepository;

    // 🌿 폴더 생성 메서드
	public void create(int workspaceId, String folderName, int createdBy) {
		Folders f = new Folders();
		f.setWorkspaceId(workspaceId);
		f.setFolderName(folderName);
		f.setCreatedBy(createdBy);
		this.folderRepository.save(f);
	}

}
