package com.jobcho.git;

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
public class Commit {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_commit")
	@SequenceGenerator(name = "seq_commit", sequenceName = "SEQ_COMMIT", allocationSize = 1)
	private Integer commitId;
	
	@ManyToOne
	@JoinColumn(name = "branch_id")
	private Branch branch;
	
	@Column
	private String content;
	
}
