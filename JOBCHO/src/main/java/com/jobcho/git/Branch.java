package com.jobcho.git;

import com.jobcho.user.Users;
import com.jobcho.workspace.Workspaces;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Branch {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_branch")
	@SequenceGenerator(name = "seq_branch", sequenceName = "SEQ_BRANCH", allocationSize = 1)
	private Integer branchId;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private Users user;
	
	@ManyToOne
	@JoinColumn(name = "workspace_id")
	private Workspaces workspace;
	
	@Column
	private String color;
	
	@Column
	private Integer countOfCommits;
	
}
