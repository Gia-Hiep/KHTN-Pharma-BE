package com.pharmacy.chat_service.repository;

import com.pharmacy.chat_service.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {

    /**
     * Lấy tin nhắn theo conversation (mới nhất trước)
     */
    List<Message> findByConversationIdAndIsDeletedFalseOrderByCreatedAtDesc(
            Long conversationId, Pageable pageable);

    /**
     * Lấy tất cả tin nhắn của conversation
     */
    List<Message> findByConversationIdAndIsDeletedFalseOrderByCreatedAtAsc(Long conversationId);

    /**
     * Đếm tin nhắn chưa đọc
     */
    @Query("""
            select count(m) from Message m
            where m.conversationId = :conversationId
              and m.senderId != :userId
              and m.readAt is null
              and m.isDeleted = false
            """)
    Long countUnreadMessages(Long conversationId, Long userId);

    /**
     * Đánh dấu đã đọc tất cả tin nhắn trong conversation
     */
    @Modifying
    @Query("""
            update Message m
            set m.readAt = :readAt
            where m.conversationId = :conversationId
              and m.senderId != :userId
              and m.readAt is null
            """)
    int markAsRead(Long conversationId, Long userId, LocalDateTime readAt);

    /**
     * Tìm tin nhắn theo nội dung
     */
    @Query("""
            select m from Message m
            where m.conversationId = :conversationId
              and lower(m.content) like lower(concat('%', :keyword, '%'))
              and m.isDeleted = false
            order by m.createdAt desc
            """)
    List<Message> searchInConversation(Long conversationId, String keyword);
}
