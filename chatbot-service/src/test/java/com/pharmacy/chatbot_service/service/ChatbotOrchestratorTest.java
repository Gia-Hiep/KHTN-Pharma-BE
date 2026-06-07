package com.pharmacy.chatbot_service.service;

import com.pharmacy.chatbot_service.dto.ChatQueryResponse;
import com.pharmacy.chatbot_service.dto.ChatQueryResponse.OrderItem;
import com.pharmacy.chatbot_service.dto.ChatQueryResponse.ProductItem;
import com.pharmacy.chatbot_service.entity.BotQueryLog;
import com.pharmacy.chatbot_service.entity.BotSession;
import com.pharmacy.chatbot_service.repository.BotQueryLogRepo;
import com.pharmacy.chatbot_service.repository.BotSessionRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotOrchestratorTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private RagService ragService;

    @Mock
    private ApiToolService apiToolService;

    @Mock
    private QueryUnderstandingService queryUnderstandingService;

    @Mock
    private CatalogRagService catalogRagService;

    @Mock
    private BotQueryLogRepo queryLogRepo;

    @Mock
    private BotSessionRepo sessionRepo;

    private ChatbotOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new ChatbotOrchestrator(
                new GuardrailService(),
                geminiService,
                ragService,
                apiToolService,
                queryUnderstandingService,
                catalogRagService,
                queryLogRepo,
                sessionRepo
        );

        lenient().when(queryUnderstandingService.understand(any(), any(), any()))
                .thenAnswer(invocation -> QueryUnderstandingService.QueryUnderstanding.fallback(
                        invocation.getArgument(1),
                        invocation.getArgument(2)
                ));
        lenient().when(sessionRepo.findByUserIdAndStatus(anyLong(), eq("ACTIVE"))).thenReturn(Optional.empty());
        lenient().when(sessionRepo.save(any(BotSession.class))).thenAnswer(invocation -> {
            BotSession session = invocation.getArgument(0);
            if (session.getId() == null) {
                session.setId(1L);
            }
            return session;
        });
        lenient().when(queryLogRepo.save(any(BotQueryLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // CatalogRag always runs alongside API search now (Gap 2)
        lenient().when(catalogRagService.retrieveProducts(any(), eq(3))).thenReturn(List.of());
        lenient().when(apiToolService.getProductsByCandidates(any(), any())).thenReturn(List.of());
    }

    @Test
    void returnsGreetingForGreetingMessage() {
        ChatQueryResponse response = orchestrator.process("xin chao", null, null, null);

        assertThat(response.type()).isEqualTo("GREETING");
        assertThat(response.intent()).isEqualTo("GREETING");
        assertThat(response.safe()).isTrue();
        assertThat(response.orders()).isEmpty();
        assertThat(response.products()).isEmpty();
        assertThat(response.sources()).isEmpty();
        assertThat(response.suggestedActions()).isNotEmpty();
    }

    @Test
    void blocksMedicalUnsafeQuestion() {
        ChatQueryResponse response = orchestrator.process("uống bao nhiêu viên", null, null, null);

        assertThat(response.type()).isEqualTo("MEDICAL_REFUSAL");
        assertThat(response.intent()).isEqualTo("MEDICAL_UNSAFE");
        assertThat(response.safe()).isFalse();
        assertThat(response.sources()).isEmpty();
    }

    @Test
    void returnsFaqAnswerWithSourcesWhenRagConfident() {
        when(ragService.retrieve("chinh sach giao hang", 3)).thenReturn(List.of(
                new RagService.ChunkResult(1L, "Noi dung policy", 0.82, "POLICY", "Chinh sach giao hang"),
                new RagService.ChunkResult(2L, "Chunk score thap", 0.20, "FAQ", "Khong nen len source")
        ));
        when(geminiService.generate(any(), any())).thenReturn("Don hang duoc giao trong 24h den 48h.");

        ChatQueryResponse response = orchestrator.process("chinh sach giao hang", null, null, null);

        assertThat(response.type()).isEqualTo("FAQ_ANSWER");
        assertThat(response.intent()).isEqualTo("FAQ_POLICY");
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().get(0).sourceType()).isEqualTo("POLICY");
        assertThat(response.sources().get(0).title()).isEqualTo("Chinh sach giao hang");
    }

    @Test
    void routesOpeningHoursQuestionToFaqPolicy() {
        when(ragService.retrieve("gio lam viec nhu the nao", 3)).thenReturn(List.of(
                new RagService.ChunkResult(2L, "Mo cua tu 8h den 21h.", 0.78, "FAQ", "Gio lam viec")
        ));
        when(geminiService.generate(any(), any())).thenReturn("Nha thuoc mo cua tu 8h den 21h moi ngay.");

        ChatQueryResponse response = orchestrator.process("gio lam viec nhu the nao", null, null, null);

        assertThat(response.type()).isEqualTo("FAQ_ANSWER");
        assertThat(response.intent()).isEqualTo("FAQ_POLICY");
        assertThat(response.sources()).hasSize(1);
        assertThat(response.sources().get(0).title()).isEqualTo("Gio lam viec");
    }

    @Test
    void routesToProductSearchForExplicitSearchQuery() {
        when(apiToolService.searchProducts(any(), eq(null))).thenReturn(List.of(
                new ProductItem(1L, "Paracetamol 500mg", "2.000d")
        ));
        when(geminiService.generate(any(), any())).thenReturn("Tim thay Paracetamol 500mg voi gia 2.000d.");

        ChatQueryResponse response = orchestrator.process("tim paracetamol", null, null, null);

        assertThat(response.type()).isEqualTo("PRODUCT_LIST");
        assertThat(response.intent()).isEqualTo("PRODUCT_SEARCH");
        assertThat(response.products()).hasSize(1);
        assertThat(response.sources()).isNotEmpty();
        verify(apiToolService).searchProducts("paracetamol", null);
        verify(geminiService).generate(any(), any()); // Gap 1: LLM generation
    }

    @Test
    void routesShortMedicineNameToProductSearch() {
        when(apiToolService.searchProducts(any(), eq(null))).thenReturn(List.of(
                new ProductItem(1L, "Paracetamol 500mg", "2.000d")
        ));
        lenient().when(geminiService.generate(any(), any())).thenReturn("Paracetamol 500mg co san.");

        ChatQueryResponse response = orchestrator.process("Paracetamol 500mg", null, null, null);

        assertThat(response.type()).isEqualTo("PRODUCT_LIST");
        assertThat(response.intent()).isEqualTo("PRODUCT_SEARCH");
        verify(apiToolService).searchProducts("paracetamol 500mg", null);
    }

    @Test
    void routesDiseaseGroupLikeQueryToProductSearch() {
        when(apiToolService.searchProducts(any(), eq(null))).thenReturn(List.of(
                new ProductItem(2L, "Thuoc cam cum ABC", "35.000d")
        ));
        lenient().when(geminiService.generate(any(), any())).thenReturn("Tim thay Thuoc cam cum ABC.");

        ChatQueryResponse response = orchestrator.process("cam cum", null, null, null);

        assertThat(response.type()).isEqualTo("PRODUCT_LIST");
        assertThat(response.intent()).isEqualTo("PRODUCT_SEARCH");
        verify(apiToolService).searchProducts("cam cum", null);
    }

    @Test
    void stripsDiseasePrefixBeforeProductSearch() {
        when(apiToolService.searchProducts(any(), eq(null))).thenReturn(List.of(
                new ProductItem(2L, "Thuoc cam cum ABC", "35.000d")
        ));
        lenient().when(geminiService.generate(any(), any())).thenReturn("Tim thay Thuoc cam cum ABC.");

        ChatQueryResponse response = orchestrator.process("benh cam cum", null, null, null);

        assertThat(response.type()).isEqualTo("PRODUCT_LIST");
        assertThat(response.intent()).isEqualTo("PRODUCT_SEARCH");
        verify(apiToolService).searchProducts("cam cum", null);
    }

    @Test
    void usesGeminiUnderstandingNormalizedQueryForProductSearch() {
        when(queryUnderstandingService.understand(eq("toi can san pham ve benh cam cum"), any(), any()))
                .thenReturn(new QueryUnderstandingService.QueryUnderstanding(
                        "PRODUCT_SEARCH",
                        "DISEASE_GROUP",
                        "cam cum",
                        List.of("cam cum"),
                        false,
                        "GEMINI"
                ));
        when(apiToolService.searchProducts(any(), eq(null))).thenReturn(List.of(
                new ProductItem(2L, "Thuoc cam cum ABC", "35.000d")
        ));
        when(geminiService.generate(any(), any())).thenReturn("Tim thay thuoc cam cum ABC.");

        ChatQueryResponse response = orchestrator.process("toi can san pham ve benh cam cum", null, null, null);

        assertThat(response.type()).isEqualTo("PRODUCT_LIST");
        assertThat(response.intent()).isEqualTo("PRODUCT_SEARCH");
        verify(apiToolService).searchProducts("cam cum", null);
        verify(geminiService).generate(any(), any());
    }

    @Test
    void mergesApiAndCatalogRagResults() {
        // API returns product 1, CatalogRag returns product 2 (different ID)
        when(apiToolService.searchProducts("cam cum", null)).thenReturn(List.of(
                new ProductItem(1L, "Decolgen Forte", "35.000d")
        ));
        when(catalogRagService.retrieveProducts("cam cum", 3)).thenReturn(List.of(
                new CatalogRagService.ProductCandidate(2L, "CATALOG_RAG", "Theo nhom benh gan dung: Ho hap", 0.78)
        ));
        when(apiToolService.getProductsByCandidates(any(), eq(null))).thenReturn(List.of(
                new ProductItem(2L, "Paracetamol 500mg", "2.000d", "Paracetamol", "OTC", "CATALOG_RAG", "Theo nhom benh gan dung: Ho hap", null)
        ));
        when(geminiService.generate(any(), any())).thenReturn("Tim thay 2 san pham lien quan den cam cum.");

        ChatQueryResponse response = orchestrator.process("benh cam cum", null, null, null);

        assertThat(response.type()).isEqualTo("PRODUCT_LIST");
        assertThat(response.products()).hasSize(2); // merged from both sources
        assertThat(response.confidenceLevel()).isEqualTo("MEDIUM"); // has RAG contribution
        assertThat(response.sources()).anyMatch(s -> "CATALOG_RAG".equals(s.sourceType())); // Gap 3
        assertThat(response.sources()).anyMatch(s -> "API".equals(s.sourceType()));
        verify(geminiService).generate(any(), any()); // Gap 1: LLM generation
    }

    @Test
    void catalogRagOnlyWhenApiReturnsEmpty() {
        when(apiToolService.searchProducts("cam cum", null)).thenReturn(List.of());
        when(catalogRagService.retrieveProducts("cam cum", 3)).thenReturn(List.of(
                new CatalogRagService.ProductCandidate(2L, "CATALOG_RAG", "Theo nhom benh gan dung: Ho hap", 0.78)
        ));
        when(apiToolService.getProductsByCandidates(any(), eq(null))).thenReturn(List.of(
                new ProductItem(2L, "Paracetamol 500mg", "2.000d", "Paracetamol", "OTC", "CATALOG_RAG", "Theo nhom benh gan dung: Ho hap", null)
        ));
        when(geminiService.generate(any(), any())).thenReturn("Tim thay Paracetamol 500mg trong nhom benh Ho hap.");

        ChatQueryResponse response = orchestrator.process("benh cam cum", null, null, null);

        assertThat(response.type()).isEqualTo("PRODUCT_LIST");
        assertThat(response.products()).hasSize(1);
        assertThat(response.confidenceLevel()).isEqualTo("MEDIUM");
        assertThat(response.answer()).contains("Paracetamol"); // Generated by Gemini
        assertThat(response.sources()).anyMatch(s -> "CATALOG_RAG".equals(s.sourceType()));
    }

    @Test
    void fallsBackToTemplateWhenGeminiFailsForProductSearch() {
        when(apiToolService.searchProducts(any(), eq(null))).thenReturn(List.of(
                new ProductItem(1L, "Paracetamol 500mg", "2.000d")
        ));
        when(geminiService.generate(any(), any())).thenReturn(null); // Gemini unavailable

        ChatQueryResponse response = orchestrator.process("tim paracetamol", null, null, null);

        assertThat(response.type()).isEqualTo("PRODUCT_LIST");
        assertThat(response.answer()).contains("Tìm thấy"); // Fallback template
        assertThat(response.answer()).contains("paracetamol");
    }

    @Test
    void refusesWhenQueryUnderstandingFlagsMedicalUnsafe() {
        when(queryUnderstandingService.understand(eq("toi can goi y rieng cho cam cum"), any(), any()))
                .thenReturn(new QueryUnderstandingService.QueryUnderstanding(
                        "MEDICAL_UNSAFE",
                        "GENERAL",
                        "cam cum",
                        List.of("cam cum"),
                        true,
                        "GEMINI"
                ));

        ChatQueryResponse response = orchestrator.process("toi can goi y rieng cho cam cum", null, null, null);

        assertThat(response.type()).isEqualTo("MEDICAL_REFUSAL");
        assertThat(response.intent()).isEqualTo("MEDICAL_UNSAFE");
        assertThat(response.safe()).isFalse();
    }

    @Test
    void asksLoginForOrderLookupWithoutJwt() {
        ChatQueryResponse response = orchestrator.process("xem don hang cua toi", null, null, null);

        assertThat(response.type()).isEqualTo("FAQ_ANSWER");
        assertThat(response.intent()).isEqualTo("ORDER_INQUIRY");
        assertThat(response.answer()).contains("đăng nhập");
        assertThat(response.orders()).isEmpty();
        assertThat(response.products()).isEmpty();
        assertThat(response.sources()).isEmpty();
    }

    @Test
    void usesOrderIdLookupWhenMessageContainsSpecificOrder() {
        when(apiToolService.getOrderById(123L, "jwt-token")).thenReturn(List.of(
                new OrderItem(123L, "CONFIRMED", "Đã xác nhận", "100.000d")
        ));

        ChatQueryResponse response = orchestrator.process("kiem tra don #123", null, null, "jwt-token");

        assertThat(response.type()).isEqualTo("ORDER_LIST");
        assertThat(response.intent()).isEqualTo("ORDER_INQUIRY");
        assertThat(response.orders()).hasSize(1);
        assertThat(response.answer()).contains("#123");
        verify(apiToolService).getOrderById(123L, "jwt-token");
    }

    @Test
    void usesOrderIdLookupForBareOrderIdQuery() {
        when(apiToolService.getOrderById(456L, "jwt-token")).thenReturn(List.of(
                new OrderItem(456L, "SHIPPING", "Đang giao", "250.000d")
        ));

        ChatQueryResponse response = orchestrator.process("#456", null, null, "jwt-token");

        assertThat(response.type()).isEqualTo("ORDER_LIST");
        assertThat(response.intent()).isEqualTo("ORDER_INQUIRY");
        assertThat(response.orders()).hasSize(1);
        verify(apiToolService).getOrderById(456L, "jwt-token");
    }

    @Test
    void returnsIngredientPromptForQuickAction() {
        ChatQueryResponse response = orchestrator.process(null, "SEARCH_BY_ACTIVE_INGREDIENT", null, null);

        assertThat(response.type()).isEqualTo("FAQ_ANSWER");
        assertThat(response.intent()).isEqualTo("PRODUCT_SEARCH");
        assertThat(response.answer()).contains("hoạt chất paracetamol");
    }

    @Test
    void returnsTrackOrderPromptForQuickAction() {
        ChatQueryResponse response = orchestrator.process(null, "TRACK_ORDER_BY_CODE", null, null);

        assertThat(response.type()).isEqualTo("FAQ_ANSWER");
        assertThat(response.intent()).isEqualTo("ORDER_INQUIRY");
        assertThat(response.answer()).contains("#123");
    }
}
