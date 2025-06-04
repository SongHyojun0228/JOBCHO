package com.jobcho.workspace;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkspaceDomainService {

	private final WorkspaceDomainRepository workspaceDomainRepository;
	
	public void createWorkspaceDomain(Integer workspaceId, String workspaceDomain, String redirectWorkspaceDomain) {
		WorkspaceDomains workspaceDomainObj = new WorkspaceDomains();
		workspaceDomainObj.setWorkspaceId(workspaceId);
		workspaceDomainObj.setWorkspaceDomain(workspaceDomain);
		workspaceDomainObj.setRedirectWorkspaceDomain(redirectWorkspaceDomain);
		
		this.workspaceDomainRepository.save(workspaceDomainObj);
		
		System.out.println("워크스페이스 도메인 설정\n workspace_id : " + workspaceId);
		System.out.println("workspace_domain : " + workspaceDomain);
		System.out.println("redirect_workspace_domain : " + redirectWorkspaceDomain);
	}

	public String findRedirectWorkspaceDomainByWorkspaceDomain(String workspaceDomain) {
		return "1";
	}
	
}
