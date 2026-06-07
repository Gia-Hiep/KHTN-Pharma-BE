package com.pharmacy.chatbot_service.repository;

import com.pharmacy.chatbot_service.entity.BotQueryLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotQueryLogRepo extends JpaRepository<BotQueryLog, Long> {
}
