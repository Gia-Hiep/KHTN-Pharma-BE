package com.pharmacy.chat_service.service;

import com.pharmacy.chat_service.dto.*;
import com.pharmacy.chat_service.entity.Conversation;
import com.pharmacy.chat_service.entity.Message;
import com.pharmacy.chat_service.repository.ConversationRepo;
import com.pharmacy.chat_service.repository.MessageRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepo conversationRepo;
    private final MessageRepo messageRepo;

    // ===== CONVERSATIONS =====

    /**
     * Lấy danh sách conversations của user
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations(Long userId) {
        return conversationRepo.findByParticipant(userId).stream()
                .map(c -> toConversationResponse(c, userId))
                .toList();
    }

    /**
     * PHARMACIST/ADMIN: Lấy hộp thư hỗ trợ (các ticket chưa gán hoặc gán cho mình)
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getPharmacistInbox(Long pharmacistId) {
        // Find tickets where participant2 == 0 (unassigned) OR participant2 == pharmacistId
        return conversationRepo.findAll().stream()
                .filter(c -> !c.getStatus().equals("CLOSED"))
                .filter(c -> c.getParticipant2() != null && (c.getParticipant2() == 0L || c.getParticipant2().equals(pharmacistId)))
                .map(c -> toConversationResponse(c, pharmacistId))
                .sorted((a, b) -> {
                    LocalDateTime ta = a.lastMessageAt() != null ? a.lastMessageAt() : a.createdAt();
                    LocalDateTime tb = b.lastMessageAt() != null ? b.lastMessageAt() : b.createdAt();
                    return tb.compareTo(ta); // newest first
                })
                .toList();
    }

    /**
     * Tạo hoặc lấy conversation giữa 2 users
     */
    @Transactional
    public Conversation createOrGetConversation(Long userId, CreateConversationRequest req) {
        Long participantId = req.participantId();

        // Nếu có participantId cụ thể → tìm conversation đã tồn tại
        if (participantId != null) {
            var existing = conversationRepo.findByParticipants(userId, participantId);
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        // Tạo mới: participant2=0 nghĩa là chưa được nhân viên nhận (support ticket)
        Conversation c = new Conversation();
        c.setType(req.type());
        c.setParticipant1(userId);
        c.setParticipant2(participantId != null ? participantId : 0L);
        c.setTitle(req.title());
        return conversationRepo.save(c);
    }

    /**
     * Lấy conversation by ID.
     * Access rules:
     *   - participant1 (buyer/creator) → luôn được
     *   - participant2 (pharmacist/assigned) → được nếu assigned
     *   - participant2 == 0 (unassigned support ticket) → pharmacist nào cũng đọc được,
     *     và tự động assign cho pharmacist đó
     */
    @Transactional
    public Conversation getConversation(Long conversationId, Long userId) {
        Conversation c = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation không tồn tại"));

        // participant1 (buyer/creator) luôn có quyền
        if (c.getParticipant1().equals(userId)) {
            return c;
        }

        // participant2 đã assign cho user này → có quyền
        if (c.getParticipant2() != null && c.getParticipant2().equals(userId)) {
            return c;
        }

        // participant2 == 0 (unassigned support ticket) → auto-assign cho pharmacist
        if (c.getParticipant2() != null && c.getParticipant2() == 0L) {
            c.setParticipant2(userId);
            conversationRepo.save(c);
            return c;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền truy cập conversation này");
    }

    /**
     * Đóng conversation
     */
    @Transactional
    public Conversation closeConversation(Long conversationId, Long userId) {
        Conversation c = getConversation(conversationId, userId);
        c.setStatus("CLOSED");
        return conversationRepo.save(c);
    }

    // ===== MESSAGES =====

    /**
     * Gửi tin nhắn
     */
    @Transactional
    public MessageResponse sendMessage(Long userId, SendMessageRequest req) {
        // Validate conversation
        Conversation conv = getConversation(req.conversationId(), userId);

        if ("CLOSED".equals(conv.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation đã đóng");
        }

        // Tạo message
        Message msg = new Message();
        msg.setConversationId(req.conversationId());
        msg.setSenderId(userId);
        msg.setContent(req.content());
        msg.setMessageType(req.messageType() != null ? req.messageType() : "TEXT");
        msg.setAttachmentUrl(req.attachmentUrl());
        msg.setAttachmentName(req.attachmentName());

        Message saved = messageRepo.save(msg);

        // Cập nhật lastMessageAt
        conv.setLastMessageAt(LocalDateTime.now());
        conversationRepo.save(conv);

        return toMessageResponse(saved);
    }

    /**
     * Lấy tin nhắn của conversation (phân trang)
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Long conversationId, Long userId, int page, int size) {
        // Validate access
        getConversation(conversationId, userId);

        return messageRepo.findByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
                        conversationId, PageRequest.of(page, size))
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    /**
     * Lấy tất cả tin nhắn của conversation (theo thứ tự thời gian)
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getAllMessages(Long conversationId, Long userId) {
        getConversation(conversationId, userId);

        return messageRepo.findByConversationIdAndIsDeletedFalseOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    /**
     * Đánh dấu đã đọc
     */
    @Transactional
    public int markAsRead(Long conversationId, Long userId) {
        getConversation(conversationId, userId);
        return messageRepo.markAsRead(conversationId, userId, LocalDateTime.now());
    }

    /**
     * Đếm tin nhắn chưa đọc
     */
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long conversationId, Long userId) {
        return messageRepo.countUnreadMessages(conversationId, userId);
    }

    /**
     * Tìm kiếm tin nhắn
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> searchMessages(Long conversationId, Long userId, String keyword) {
        getConversation(conversationId, userId);

        return messageRepo.searchInConversation(conversationId, keyword)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    /**
     * Xóa tin nhắn (soft delete)
     */
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        Message msg = messageRepo.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message không tồn tại"));

        if (!msg.getSenderId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ người gửi mới được xóa tin nhắn");
        }

        msg.setIsDeleted(true);
        messageRepo.save(msg);
    }

    // ===== MAPPING =====

    private MessageResponse toMessageResponse(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getConversationId(),
                m.getSenderId(),
                m.getContent(),
                m.getMessageType(),
                m.getAttachmentUrl(),
                m.getAttachmentName(),
                m.getReadAt(),
                m.getCreatedAt()
        );
    }

    private ConversationResponse toConversationResponse(Conversation c, Long userId) {
        Long unread = messageRepo.countUnreadMessages(c.getId(), userId);

        // Get last message
        var messages = messageRepo.findByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
                c.getId(), PageRequest.of(0, 1));
        MessageResponse lastMsg = messages.isEmpty() ? null : toMessageResponse(messages.get(0));

        return new ConversationResponse(
                c.getId(),
                c.getType(),
                c.getParticipant1(),
                c.getParticipant2(),
                c.getTitle(),
                c.getStatus(),
                c.getLastMessageAt(),
                c.getCreatedAt(),
                unread,
                lastMsg
        );
    }
}
