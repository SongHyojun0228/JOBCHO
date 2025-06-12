package com.jobcho.cs;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CSRepository extends JpaRepository<CS, Integer> {
	@Query("SELECT c FROM CS c WHERE c.csChatroomId = :csChatroomId ORDER BY c.csId")
	List<CS> findByCsChatroomIdOrdered(@Param("csChatroomId") Integer csChatroomId);
}
