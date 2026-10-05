package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.AIConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AIConversationRepository
        extends JpaRepository<AIConversation, Long> {

    List<AIConversation> findByUserIdOrderByPinnedDescCreatedAtDesc(
            Integer userId
    );
}