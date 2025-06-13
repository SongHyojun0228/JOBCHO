package com.jobcho.mention;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MentionService {

    private final MentionRepository mentionRepository;

    // 🌿 멘션 저장 메서드
    public void saveMention(Integer chatroomId, Integer senderId, Integer receiverId) {
        Mentions mention = new Mentions();
        mention.setChatroomId(chatroomId);
        mention.setSenderId(senderId);
        mention.setReceiverId(receiverId);
        mentionRepository.save(mention);
        
        System.out.println(" >>>> mention save1");
    }
}

