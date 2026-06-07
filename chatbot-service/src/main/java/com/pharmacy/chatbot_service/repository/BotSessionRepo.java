package com.pharmacy.chatbot_service.repository;

import com.pharmacy.chatbot_service.entity.BotSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BotSessionRepo extends JpaRepository<BotSession, Long> {

    Optional<BotSession> findBySessionTokenAndStatus(String sessionToken, String status);

    Optional<BotSession> findByUserIdAndStatus(Long userId, String status);
}
