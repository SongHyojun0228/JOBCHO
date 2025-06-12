package com.jobcho.mention;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MentionRepository extends JpaRepository<Mentions, Integer> {
}
