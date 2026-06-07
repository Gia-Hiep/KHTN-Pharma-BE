package com.pharmacy.chatbot_service.repository;

import com.pharmacy.chatbot_service.entity.BotMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotMessageRepo extends JpaRepository<BotMessage, Long> {

    List<BotMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<BotMessage> findBySessionIdOrderByCreatedAtDesc(Long sessionId);
}
