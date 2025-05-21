package com.jobcho.workspace;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class FolderService {

	private final FolderRepository folderRepository;

	public void create(int workspaceId, String folderName, int createdBy) {
		Folders f = new Folders();
		f.setWorkspaceId(workspaceId);
		f.setFolderName(folderName);
		f.setCreatedBy(createdBy);
		this.folderRepository.save(f);
	}

}
