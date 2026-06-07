package com.pharmacy.chatbot_service.repository;

import com.pharmacy.chatbot_service.entity.BotCatalogChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotCatalogChunkRepo extends JpaRepository<BotCatalogChunk, Long> {
    List<BotCatalogChunk> findByActiveTrueAndEmbeddingIsNotNull();
}
