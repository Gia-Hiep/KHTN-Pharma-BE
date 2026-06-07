package com.pharmacy.chatbot_service.service;

import com.pharmacy.chatbot_service.entity.BotDocument;
import com.pharmacy.chatbot_service.entity.BotDocumentChunk;
import com.pharmacy.chatbot_service.entity.BotFaq;
import com.pharmacy.chatbot_service.repository.BotDocumentChunkRepo;
import com.pharmacy.chatbot_service.repository.BotDocumentRepo;
import com.pharmacy.chatbot_service.repository.BotFaqRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private BotDocumentRepo documentRepo;

    @Mock
    private BotDocumentChunkRepo chunkRepo;

    @Mock
    private BotFaqRepo faqRepo;

    @Mock
    private GeminiService geminiService;

    private RagService ragService;

    @BeforeEach
    void setUp() {
        ragService = new RagService(
                documentRepo,
                chunkRepo,
                faqRepo,
                geminiService,
                new GuardrailService()
        );
    }

    @Test
    void retrieveIgnoresInactiveDocumentChunks() {
        BotDocument activeDoc = new BotDocument();
        activeDoc.setId(1L);
        activeDoc.setTitle("Chinh sach giao hang");
        activeDoc.setSourceType("POLICY");
        activeDoc.setContent("Noi dung");

        BotFaq activeFaq = new BotFaq();
        activeFaq.setId(7L);
        activeFaq.setQuestion("Gio lam viec");
        activeFaq.setAnswer("8h - 21h");
        activeFaq.setActive(true);

        BotDocumentChunk activeDocChunk = new BotDocumentChunk();
        activeDocChunk.setId(11L);
        activeDocChunk.setDocumentId(1L);
        activeDocChunk.setSourceType("POLICY");
        activeDocChunk.setSourceTitle("Chinh sach giao hang");
        activeDocChunk.setContent("Noi dung giao hang");
        activeDocChunk.setEmbedding("[1,0]");

        BotDocumentChunk inactiveDocChunk = new BotDocumentChunk();
        inactiveDocChunk.setId(12L);
        inactiveDocChunk.setDocumentId(2L);
        inactiveDocChunk.setSourceType("POLICY");
        inactiveDocChunk.setSourceTitle("Inactive");
        inactiveDocChunk.setContent("Khong duoc dung");
        inactiveDocChunk.setEmbedding("[1,0]");

        BotDocumentChunk activeFaqChunk = new BotDocumentChunk();
        activeFaqChunk.setId(13L);
        activeFaqChunk.setFaqId(7L);
        activeFaqChunk.setSourceType("FAQ");
        activeFaqChunk.setSourceTitle("Gio lam viec");
        activeFaqChunk.setContent("FAQ gio lam viec");
        activeFaqChunk.setEmbedding("[1,0]");

        when(documentRepo.findByActiveTrue()).thenReturn(List.of(activeDoc));
        when(faqRepo.findAllActive()).thenReturn(List.of(activeFaq));
        when(chunkRepo.findByEmbeddingIsNotNull()).thenReturn(List.of(activeDocChunk, inactiveDocChunk, activeFaqChunk));
        when(geminiService.jsonToEmbedding(any())).thenReturn(new double[]{1.0, 0.0});
        when(geminiService.embed("giao hang")).thenReturn(new double[]{1.0, 0.0});

        ragService.loadEmbeddingsToCache();
        List<RagService.ChunkResult> results = ragService.retrieve("giao hang", 5);

        assertThat(results).hasSize(2);
        assertThat(results).extracting(RagService.ChunkResult::sourceTitle)
                .containsExactlyInAnyOrder("Chinh sach giao hang", "Gio lam viec");
        assertThat(results).extracting(RagService.ChunkResult::sourceTitle)
                .doesNotContain("Inactive");
    }

    @Test
    void importDocumentRejectsUnsafeMedicalContent() {
        assertThatThrownBy(() -> ragService.importDocument(
                "Huong dan lieu dung",
                "GUIDE",
                "Thuoc nay uong bao nhieu vien moi ngay?"
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tai lieu co noi dung y te nhay cam");
    }
}
