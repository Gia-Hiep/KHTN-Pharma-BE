package com.pharmacy.chat_service.controller;

import com.pharmacy.chat_service.dto.*;
import com.pharmacy.chat_service.entity.Conversation;
import com.pharmacy.chat_service.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ===== CONVERSATIONS =====

    /**
     * Lấy danh sách conversations của user hiện tại
     */
    @GetMapping("/conversations")
    public List<ConversationResponse> getMyConversations(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.getMyConversations(userId);
    }

    /**
     * PHARMACIST/ADMIN: Inbox hỗ trợ khách hàng
     */
    @GetMapping("/support/inbox")
    public List<ConversationResponse> getPharmacistInbox(Authentication auth) {
        Long pharmacistId = (Long) auth.getPrincipal();
        return chatService.getPharmacistInbox(pharmacistId);
    }

    /**
     * Tạo hoặc lấy conversation
     */
    @PostMapping("/conversations")
    public Conversation createOrGetConversation(
            @Valid @RequestBody CreateConversationRequest req,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.createOrGetConversation(userId, req);
    }

    /**
     * Lấy chi tiết conversation
     */
    @GetMapping("/conversations/{id}")
    public Conversation getConversation(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.getConversation(id, userId);
    }

    /**
     * Đóng conversation
     */
    @PostMapping("/conversations/{id}/close")
    public Conversation closeConversation(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.closeConversation(id, userId);
    }

    // ===== MESSAGES =====

    /**
     * Gửi tin nhắn
     */
    @PostMapping("/messages")
    public MessageResponse sendMessage(
            @Valid @RequestBody SendMessageRequest req,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.sendMessage(userId, req);
    }

    /**
     * Lấy tin nhắn của conversation (phân trang, mới nhất trước)
     */
    @GetMapping("/conversations/{id}/messages")
    public List<MessageResponse> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.getMessages(id, userId, page, size);
    }

    /**
     * Lấy tất cả tin nhắn (theo thứ tự thời gian tăng dần)
     */
    @GetMapping("/conversations/{id}/messages/all")
    public List<MessageResponse> getAllMessages(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.getAllMessages(id, userId);
    }

    /**
     * Đánh dấu đã đọc
     */
    @PostMapping("/conversations/{id}/read")
    public int markAsRead(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.markAsRead(id, userId);
    }

    /**
     * Đếm tin nhắn chưa đọc
     */
    @GetMapping("/conversations/{id}/unread")
    public Long getUnreadCount(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.getUnreadCount(id, userId);
    }

    /**
     * Tìm kiếm tin nhắn
     */
    @GetMapping("/conversations/{id}/search")
    public List<MessageResponse> searchMessages(
            @PathVariable Long id,
            @RequestParam String q,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        return chatService.searchMessages(id, userId, q);
    }

    /**
     * Xóa tin nhắn
     */
    @DeleteMapping("/messages/{messageId}")
    public void deleteMessage(@PathVariable Long messageId, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        chatService.deleteMessage(messageId, userId);
    }
}
