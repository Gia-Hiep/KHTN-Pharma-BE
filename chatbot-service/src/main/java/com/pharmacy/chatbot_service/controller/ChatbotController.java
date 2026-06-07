package com.pharmacy.chatbot_service.controller;

import com.pharmacy.chatbot_service.dto.*;
import com.pharmacy.chatbot_service.entity.BotFaq;
import com.pharmacy.chatbot_service.entity.BotFeedback;
import com.pharmacy.chatbot_service.service.ChatbotOrchestrator;
import com.pharmacy.chatbot_service.service.ChatbotService;
import com.pharmacy.chatbot_service.service.CatalogRagService;
import com.pharmacy.chatbot_service.service.RagService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;
    private final ChatbotOrchestrator orchestrator;
    private final RagService ragService;
    private final CatalogRagService catalogRagService;

    // ===== POST /chatbot/message — Gửi tin nhắn cho chatbot =====

    @Deprecated(since = "phase-1-cleanup")
    @PostMapping("/message")
    public ChatResponse sendMessage(
            @Valid @RequestBody ChatRequest req,
            Authentication auth
    ) {
        Long userId = (auth != null) ? (Long) auth.getPrincipal() : null;
        return chatbotService.chat(req, userId);
    }

    // ===== GET /chatbot/session/{sessionId} — Lấy context session =====

    @GetMapping("/session/{sessionId}")
    public SessionResponse getSession(@PathVariable Long sessionId) {
        return chatbotService.getSessionById(sessionId);
    }

    /** Kết thúc session */
    @PostMapping("/session/end")
    public void endSession(
            @RequestParam(required = false) String sessionToken,
            Authentication auth
    ) {
        Long userId = (auth != null) ? (Long) auth.getPrincipal() : null;
        chatbotService.endSession(sessionToken, userId);
    }

    // ===== GET /chatbot/faq — Lấy tất cả FAQ =====

    @GetMapping("/faq")
    public List<BotFaq> getAllFaqs() {
        return chatbotService.getAllFaqs();
    }

    // ===== GET /chatbot/faq/search — Tìm FAQ =====

    @GetMapping("/faq/search")
    public List<BotFaq> searchFaq(@RequestParam String q) {
        return chatbotService.searchFaqs(q);
    }

    // ===== POST /chatbot/faq — Thêm FAQ =====

    @PostMapping("/faq")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public BotFaq createFaq(@Valid @RequestBody CreateFaqRequest req) {
        return chatbotService.createFaq(req);
    }

    @PutMapping("/faq/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PHARMACIST')")
    public BotFaq updateFaq(@PathVariable Long id, @Valid @RequestBody CreateFaqRequest req) {
        return chatbotService.updateFaq(id, req);
    }

    @DeleteMapping("/faq/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteFaq(@PathVariable Long id) {
        chatbotService.deleteFaq(id);
    }

    // ===== POST /chatbot/feedback — User feedback =====

    @PostMapping("/feedback")
    public BotFeedback submitFeedback(
            @Valid @RequestBody FeedbackRequest req,
            Authentication auth
    ) {
        Long userId = (auth != null) ? (Long) auth.getPrincipal() : null;
        return chatbotService.saveFeedback(req, userId);
    }

    // ===== POST /chatbot/query — AI Chatbot (RAG + Gemini) =====

    @PostMapping("/query")
    public ChatQueryResponse query(
            @RequestBody ChatRequest req,
            Authentication auth,
            HttpServletRequest httpRequest
    ) {
        Long userId = (auth != null) ? (Long) auth.getPrincipal() : null;
        String authHeader = httpRequest.getHeader("Authorization");
        String jwtToken = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        return orchestrator.process(req.message(), req.actionId(), userId, jwtToken);
    }

    // ===== POST /chatbot/documents/import — Import RAG document =====

    @PostMapping("/documents/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> importDocument(@RequestBody Map<String, String> req) {
        int chunks = ragService.importDocument(
                req.get("title"),
                req.get("sourceType"),
                req.get("content")
        );
        return Map.of("status", "imported", "chunksCreated", chunks);
    }

    // ===== POST /chatbot/reindex — Re-embed all documents =====

    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> reindex() {
        int total = ragService.reindexAll();
        return Map.of("status", "completed", "totalChunks", total);
    }

    // ===== POST /chatbot/catalog/reindex — Rebuild product/category/disease-group RAG index =====

    @PostMapping("/catalog/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> reindexCatalog(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        String jwtToken = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
        int total = catalogRagService.reindexCatalog(jwtToken);
        return Map.of("status", "completed", "totalChunks", total);
    }
}
