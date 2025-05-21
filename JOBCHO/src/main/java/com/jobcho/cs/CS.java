package com.jobcho.cs;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

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

@Getter
@Setter
@Entity
@DynamicInsert
public class CS {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_cs")
	@SequenceGenerator(name = "seq_cs", sequenceName = "SEQ_CS", allocationSize = 1)
	private Integer csId;

	@ManyToOne
	@JoinColumn(name = "sender_id")
	private Users sender;

	@ManyToOne
	@JoinColumn(name = "receiver_id")
	private Users receiver;

	@Column
	private String content;

	@Column(name = "sent_date", insertable = false)
	private LocalDateTime sentDate;

	@Column
	@ColumnDefault("0")
	private Integer isRead;

	@Column
	@ColumnDefault("0")
	private Integer isEdited;
	
	@Column
	@ColumnDefault("0")
	private Integer isDeleted;
	
	@ManyToOne
	@JoinColumn(name = "cschatroom_id")
	private CsChatroom csChatroomId;

}
