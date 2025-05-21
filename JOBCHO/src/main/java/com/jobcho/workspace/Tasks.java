package com.jobcho.workspace;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Tasks {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_task")
	@SequenceGenerator(name = "seq_task", sequenceName = "SEQ_TASK", allocationSize = 1)
	private Integer taskId;
	
	@Column
	private Integer authorId;
	
	@Column
	private Integer chatroomId;
	
	@Column
	private String taskTitle;
	
	@Column
	private String description;
	
	@Column
	private Integer status;
	
	@Column(name = "created_date", insertable = false)
	private LocalDateTime createdDate;
	
	@Column
	private LocalDateTime startDate;
	
	@Column
	private LocalDateTime endDate;
	
	@Column
	private Integer mychatroomId;
}
