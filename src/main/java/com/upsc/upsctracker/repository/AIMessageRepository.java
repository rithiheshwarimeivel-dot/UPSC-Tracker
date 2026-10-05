package com.upsc.upsctracker.repository;

import com.upsc.upsctracker.entity.AIMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIMessageRepository
        extends JpaRepository<AIMessage, Long> {

    // =========================================================
    // GET ALL MESSAGES OF A CONVERSATION
    // =========================================================

    List<AIMessage> findByConversationIdOrderByCreatedAtAsc(
            Long conversationId
    );

    // =========================================================
    // DELETE ALL MESSAGES OF A CONVERSATION
    // =========================================================

    void deleteByConversationId(
            Long conversationId
    );
}