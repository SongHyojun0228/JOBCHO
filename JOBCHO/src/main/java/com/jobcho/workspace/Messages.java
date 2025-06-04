package com.jobcho.workspace;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import com.jobcho.member.Members;

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
@DynamicInsert
public class Messages {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_message")
	@SequenceGenerator(name = "seq_message", sequenceName = "SEQ_MESSAGE", allocationSize = 1)
	private Integer messageId;
	
	@Column
	private Integer chatroomId;
	
	@Column
	private Integer senderId;
	
	@Column
	private String content;
	
	@Column(name = "created_date", insertable = false)
	private LocalDateTime createdDate;
	
	@Column
	private Integer isEdited;
	
	@Column
	private Integer isDeleted;
}
