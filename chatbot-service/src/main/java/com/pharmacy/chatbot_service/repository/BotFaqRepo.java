package com.pharmacy.chatbot_service.repository;

import com.pharmacy.chatbot_service.entity.BotFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BotFaqRepo extends JpaRepository<BotFaq, Long> {

    List<BotFaq> findByActiveTrueOrderByPriorityDesc();

    List<BotFaq> findByIntentAndActiveTrue(String intent);

    @Query("""
            select f from BotFaq f
            where f.active = true
            order by f.priority desc
            """)
    List<BotFaq> findAllActive();
}
