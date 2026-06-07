package com.pharmacy.chatbot_service.service;

import com.pharmacy.chatbot_service.entity.BotDocument;
import com.pharmacy.chatbot_service.repository.BotDocumentChunkRepo;
import com.pharmacy.chatbot_service.repository.BotDocumentRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseBootstrapServiceTest {

    @Mock
    private BotDocumentRepo documentRepo;

    @Mock
    private BotDocumentChunkRepo chunkRepo;

    @Mock
    private RagService ragService;

    @Test
    void bootstrapUpsertsCuratedDocsAndDisablesLegacyCorpus() {
        GuardrailService guardrailService = new GuardrailService();

        BotDocument shippingDoc = new BotDocument();
        shippingDoc.setId(1L);
        shippingDoc.setTitle("Chinh sach giao hang");
        shippingDoc.setSourceType("POLICY");
        shippingDoc.setContent("Noi dung cu");
        shippingDoc.setActive(true);

        BotDocument loyaltyDoc = new BotDocument();
        loyaltyDoc.setId(2L);
        loyaltyDoc.setTitle("Chuong trinh loyalty");
        loyaltyDoc.setSourceType("POLICY");
        loyaltyDoc.setContent("Noi dung loyalty cu");
        loyaltyDoc.setActive(true);

        List<BotDocument> documents = new ArrayList<>(List.of(shippingDoc, loyaltyDoc));
        AtomicLong idSequence = new AtomicLong(10L);

        when(documentRepo.findAll()).thenAnswer(invocation -> new ArrayList<>(documents));
        when(documentRepo.save(any(BotDocument.class))).thenAnswer(invocation -> {
            BotDocument doc = invocation.getArgument(0);
            if (doc.getId() == null) {
                doc.setId(idSequence.getAndIncrement());
                documents.add(doc);
            }
            return doc;
        });

        KnowledgeBaseBootstrapService bootstrapService = new KnowledgeBaseBootstrapService(
                documentRepo,
                chunkRepo,
                guardrailService,
                ragService
        );

        bootstrapService.bootstrap();

        BotDocument updatedShippingDoc = documents.stream()
                .filter(doc -> "Chinh sach giao hang".equals(doc.getTitle()))
                .findFirst()
                .orElseThrow();
        BotDocument disabledLoyaltyDoc = documents.stream()
                .filter(doc -> "Chuong trinh loyalty".equals(doc.getTitle()))
                .findFirst()
                .orElseThrow();

        assertThat(updatedShippingDoc.getContent()).contains("Phi van chuyen");
        assertThat(disabledLoyaltyDoc.getActive()).isFalse();
        assertThat(documents).extracting(BotDocument::getTitle)
                .contains("Huong dan dat hang", "Huong dan tra cuu don hang", "Gioi thieu he thong");

        verify(chunkRepo).deleteByDocumentId(1L);
        verify(ragService).loadEmbeddingsToCache();
    }
}
