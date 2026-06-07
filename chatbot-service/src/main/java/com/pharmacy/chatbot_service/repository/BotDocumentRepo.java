package com.pharmacy.chatbot_service.repository;

import com.pharmacy.chatbot_service.entity.BotDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotDocumentRepo extends JpaRepository<BotDocument, Long> {
    List<BotDocument> findByActiveTrue();
    List<BotDocument> findBySourceTypeAndActiveTrue(String sourceType);
}
