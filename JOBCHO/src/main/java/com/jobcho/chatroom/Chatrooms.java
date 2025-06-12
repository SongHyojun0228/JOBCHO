package com.jobcho.chatroom;

import java.time.LocalDateTime;
import java.util.List;

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
public class Chatrooms {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_chatroom")
	@SequenceGenerator(name = "seq_chatroom", sequenceName = "SEQ_CHATROOM", allocationSize = 1)
	private Integer chatroomId;
	
	@Column
	private Integer folderId;
	
	@Column
	private Integer createdBy;
	
	@Column
	private String chatroomName;
	
	@Column
	private String description;
	
	@Column
	private Integer isPrivate;
	
	@Column(name = "created_date", insertable = false)
	private LocalDateTime createdDate;
}
