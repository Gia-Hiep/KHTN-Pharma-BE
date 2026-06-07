package com.pharmacy.chatbot_service.repository;

import com.pharmacy.chatbot_service.entity.BotFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotFeedbackRepo extends JpaRepository<BotFeedback, Long> {
    List<BotFeedback> findBySessionId(Long sessionId);
}
