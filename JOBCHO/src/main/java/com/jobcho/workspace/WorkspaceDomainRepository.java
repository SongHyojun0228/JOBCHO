package com.jobcho.workspace;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceDomainRepository extends JpaRepository<WorkspaceDomains, Integer>{

    Optional<WorkspaceDomains> findByWorkspaceDomain(String customDomain);

}
