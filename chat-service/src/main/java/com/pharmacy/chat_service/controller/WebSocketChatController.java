package com.pharmacy.chat_service.controller;

import com.pharmacy.chat_service.dto.MessageResponse;
import com.pharmacy.chat_service.dto.SendMessageRequest;
import com.pharmacy.chat_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Nhận tin nhắn từ client và broadcast đến conversation
     * Client gửi đến: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest req, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        MessageResponse msg = chatService.sendMessage(userId, req);

        // Gửi tin nhắn đến topic của conversation
        messagingTemplate.convertAndSend(
                "/topic/conversation." + req.conversationId(),
                msg
        );
    }

    /**
     * Thông báo user đang gõ
     * Client gửi đến: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingNotification notification, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        notification = new TypingNotification(
                notification.conversationId(),
                userId,
                notification.isTyping()
        );

        messagingTemplate.convertAndSend(
                "/topic/conversation." + notification.conversationId() + ".typing",
                notification
        );
    }

    /**
     * Đánh dấu đã đọc qua WebSocket
     * Client gửi đến: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(@Payload ReadNotification notification, Principal principal) {
        Long userId = Long.parseLong(principal.getName());
        chatService.markAsRead(notification.conversationId(), userId);

        // Thông báo cho người gửi rằng tin nhắn đã được đọc
        messagingTemplate.convertAndSend(
                "/topic/conversation." + notification.conversationId() + ".read",
                new ReadNotification(notification.conversationId(), userId)
        );
    }

    // DTOs nội bộ cho WebSocket
    public record TypingNotification(Long conversationId, Long userId, boolean isTyping) {}
    public record ReadNotification(Long conversationId, Long userId) {}
}
