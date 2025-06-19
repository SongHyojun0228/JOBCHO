package com.jobcho.message;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;

import com.jobcho.file.Files;
import com.jobcho.user.Users;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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

	@ManyToOne
	@JoinColumn(name = "sender_id")
	private Users sender;

	@Column
	private String content;

	@Column(name = "created_date", insertable = false)
	private LocalDateTime createdDate;

	@Column
	private Integer isEdited;

	@Column
	private Integer isDeleted;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	private Messages parentMessage;

	@OneToMany(mappedBy = "parentMessage", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Messages> replies = new ArrayList<>();

	@OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Files> files = new ArrayList<>();

}
