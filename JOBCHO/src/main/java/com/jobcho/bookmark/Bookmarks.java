package com.jobcho.bookmark;

import com.jobcho.user.Users;

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
public class Bookmarks {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_bookmark")
	@SequenceGenerator(name = "seq_bookmark", sequenceName = "SEQ_BOOKMARK", allocationSize = 1)
	private Integer bookmarkId;
	
	@ManyToOne
	@JoinColumn(name = "user_id")  
	private Users user;
	
	@Column
	private Integer messageId;
	
	@Column
	private Integer notificationId;
	
	@Column
	private Integer voteId;
	
	@Column
	private Integer taskId;
	
	@Column
	private Integer chatroomId;
	
	@Column
	private Integer fileId;
	
	@Column
	private Integer myChatroomId;
	
}
