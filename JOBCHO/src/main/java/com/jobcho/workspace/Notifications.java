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
public class Notifications {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_notification")
	@SequenceGenerator(name = "seq_notification", sequenceName = "SEQ_NOTIFICATION", allocationSize = 1)
	private Integer notificationId;

	@Column
	private Integer authorId;

	@Column
	private Integer chatroomId;

	@Column
	private String content;

	@Column(name = "created_date", insertable = false)
	private LocalDateTime createdDate;

	@Column
	private Integer isEdited;

	@Column
	private Integer isDeleted;

	@Column
	private String Title;
}
