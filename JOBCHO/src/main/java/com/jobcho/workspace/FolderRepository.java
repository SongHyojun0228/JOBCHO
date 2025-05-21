package com.jobcho.workspace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FolderRepository extends JpaRepository<Folders, Integer>{
	List<Folders> findByWorkspaceId(int workspaceId);
}
