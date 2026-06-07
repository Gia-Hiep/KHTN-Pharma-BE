package com.pharmacy.chatbot_service.repository;

import com.pharmacy.chatbot_service.entity.BotDocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotDocumentChunkRepo extends JpaRepository<BotDocumentChunk, Long> {
    List<BotDocumentChunk> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);
    List<BotDocumentChunk> findByEmbeddingIsNotNull();
}
