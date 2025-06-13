package com.jobcho.mention;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Mentions {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_mention")
	@SequenceGenerator(name = "seq_mention", sequenceName = "SEQ_MENTION", allocationSize = 1)
	private Integer mentionId;
	
	@Column
	private Integer chatroomId;

	@Column
	private Integer senderId;
	
	@Column
	private Integer receiverId;
	
	@Column(name = "created_date", insertable = false)
	private LocalDateTime createdDate;
	
}
