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
public class GitFolder {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_git_folder")
	@SequenceGenerator(name = "seq_git_folder", sequenceName = "SEQ_GIT_FORDER", allocationSize = 1)
	private Integer folderId;

	@ManyToOne
	@JoinColumn(name = "higher_folder")
	private GitFolder gitFolder;

	@ManyToOne
	@JoinColumn(name = "commit_id")
	private Commit commit;

	@Column
	private String folderName;

}
