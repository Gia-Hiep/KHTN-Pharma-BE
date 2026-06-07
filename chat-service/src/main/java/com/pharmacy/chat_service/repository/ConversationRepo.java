package com.pharmacy.chat_service.repository;

import com.pharmacy.chat_service.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    /**
     * Tìm tất cả conversation mà user tham gia
     */
    @Query("""
            select c from Conversation c
            where c.participant1 = :userId or c.participant2 = :userId
            order by c.lastMessageAt desc nulls last
            """)
    List<Conversation> findByParticipant(Long userId);

    /**
     * Tìm conversation giữa 2 user
     */
    @Query("""
            select c from Conversation c
            where (c.participant1 = :user1 and c.participant2 = :user2)
               or (c.participant1 = :user2 and c.participant2 = :user1)
            """)
    Optional<Conversation> findByParticipants(Long user1, Long user2);

    /**
     * Tìm theo type
     */
    List<Conversation> findByTypeOrderByLastMessageAtDesc(String type);

    /**
     * Tìm theo status
     */
    List<Conversation> findByStatusOrderByLastMessageAtDesc(String status);
}
