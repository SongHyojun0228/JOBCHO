package com.jobcho.alarm;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlarmRepository extends JpaRepository<Alarms, Integer> {

	@Query("SELECT a FROM Alarms a WHERE a.user.userId = :userId")
	List<Alarms> findByUserId(@Param("userId") Integer userId);

	@Query("SELECT a.workspaceId AS workspaceId, COUNT(a) AS cnt " + "FROM Alarms a " + "WHERE a.user.userId = :userId "
			+ "GROUP BY a.workspaceId")
	List<WorkspaceAlarmCount> countAlarmsGroupedByWorkspace(@Param("userId") Integer userId);

	@Query("SELECT a FROM Alarms a WHERE a.user.userId = :userId AND a.workspaceId = :workspaceId ORDER BY a.createdDate DESC")
	List<Alarms> findByWorkspaceId(@Param("userId") Integer userId, @Param("workspaceId") Integer workspaceId);
}
