package com.pharmacy.chatbot_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.chatbot_service.dto.ChatQueryResponse;
import com.pharmacy.chatbot_service.exception.ApiExceptionHandler;
import com.pharmacy.chatbot_service.service.ChatbotOrchestrator;
import com.pharmacy.chatbot_service.service.ChatbotService;
import com.pharmacy.chatbot_service.service.CatalogRagService;
import com.pharmacy.chatbot_service.service.RagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatbotControllerTest {

    private final ChatbotService chatbotService = mock(ChatbotService.class);
    private final ChatbotOrchestrator orchestrator = mock(ChatbotOrchestrator.class);
    private final RagService ragService = mock(RagService.class);
    private final CatalogRagService catalogRagService = mock(CatalogRagService.class);

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ChatbotController controller = new ChatbotController(chatbotService, orchestrator, ragService, catalogRagService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void queryReturnsFaqContractWithSources() throws Exception {
        when(orchestrator.process(eq("chinh sach giao hang"), eq(null), eq(null), eq(null)))
                .thenReturn(new ChatQueryResponse(
                        "FAQ_ANSWER",
                        "Thong tin",
                        "Don hang duoc xu ly sau khi xac nhan.",
                        "FAQ_POLICY",
                        "HIGH",
                        true,
                        List.of(),
                        List.of(),
                        List.of(new ChatQueryResponse.Source("Chinh sach giao hang", "POLICY")),
                        List.of(new ChatQueryResponse.Action("SEARCH_PRODUCT", "Tim san pham")),
                        10L
                ));

        mockMvc.perform(post("/chatbot/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "chinh sach giao hang",
                                  "actionId": null,
                                  "sessionId": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("FAQ_ANSWER"))
                .andExpect(jsonPath("$.intent").value("FAQ_POLICY"))
                .andExpect(jsonPath("$.sources[0].title").value("Chinh sach giao hang"))
                .andExpect(jsonPath("$.sources[0].sourceType").value("POLICY"))
                .andExpect(jsonPath("$.orders").isArray())
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.suggestedActions[0].id").value("SEARCH_PRODUCT"));
    }

    @Test
    void queryReturnsOrderContractAndForwardsAuth() throws Exception {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(9L, null);

        when(orchestrator.process(any(), any(), any(), any()))
                .thenReturn(new ChatQueryResponse(
                        "ORDER_LIST",
                        "Đơn hàng của bạn",
                        "Thông tin đơn hàng #123: Đang giao - Đã thanh toán",
                        "ORDER_INQUIRY",
                        "HIGH",
                        true,
                        List.of(new ChatQueryResponse.OrderItem(
                                123L,
                                "SHIPPING",
                                "Đang giao",
                                "150.000d",
                                "PAID",
                                "Đã thanh toán",
                                "09:00 25/04/2026",
                                "VN123456",
                                null
                        )),
                        List.of(),
                        List.of(),
                        List.of(new ChatQueryResponse.Action("TRACK_ORDER_BY_CODE", "Tra đơn theo mã")),
                        22L
                ));

        mockMvc.perform(post("/chatbot/query")
                        .principal(authentication)
                        .header("Authorization", "Bearer jwt-demo-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "kiem tra don #123",
                                  "actionId": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ORDER_LIST"))
                .andExpect(jsonPath("$.orders[0].id").value(123))
                .andExpect(jsonPath("$.orders[0].paymentStatusLabel").value("Đã thanh toán"))
                .andExpect(jsonPath("$.orders[0].trackingCode").value("VN123456"));

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> jwtCaptor = ArgumentCaptor.forClass(String.class);
        verify(orchestrator).process(eq("kiem tra don #123"), eq(null), userIdCaptor.capture(), jwtCaptor.capture());
        assertThat(userIdCaptor.getValue()).isEqualTo(9L);
        assertThat(jwtCaptor.getValue()).isEqualTo("jwt-demo-token");
    }

    @Test
    void queryReturnsProductContract() throws Exception {
        when(orchestrator.process(eq("hoat chat paracetamol"), eq(null), eq(null), eq(null)))
                .thenReturn(new ChatQueryResponse(
                        "PRODUCT_LIST",
                        "Sản phẩm tìm thấy",
                        "Tìm thấy 1 sản phẩm - Theo hoạt chất:",
                        "PRODUCT_SEARCH",
                        "HIGH",
                        true,
                        List.of(),
                        List.of(new ChatQueryResponse.ProductItem(
                                1L,
                                "Paracetamol 500mg",
                                "2.000d",
                                "Paracetamol",
                                "Thuoc giam dau, ha sot thong dung",
                                "Uong 1-2 vien moi 4-6 gio khi can",
                                "Hiem gap: di ung, phat ban",
                                "OTC",
                                "ACTIVE_INGREDIENT",
                                "Theo hoat chat",
                                null
                        )),
                        List.of(),
                        List.of(new ChatQueryResponse.Action("SEARCH_BY_CATEGORY", "Tìm theo danh mục")),
                        30L
                ));

        mockMvc.perform(post("/chatbot/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "hoat chat paracetamol",
                                  "actionId": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PRODUCT_LIST"))
                .andExpect(jsonPath("$.products[0].name").value("Paracetamol 500mg"))
                .andExpect(jsonPath("$.products[0].activeIngredient").value("Paracetamol"))
                .andExpect(jsonPath("$.products[0].description").value("Thuoc giam dau, ha sot thong dung"))
                .andExpect(jsonPath("$.products[0].usageInstructions").value("Uong 1-2 vien moi 4-6 gio khi can"))
                .andExpect(jsonPath("$.products[0].sideEffects").value("Hiem gap: di ung, phat ban"))
                .andExpect(jsonPath("$.products[0].categoryName").value("OTC"))
                .andExpect(jsonPath("$.products[0].matchLabel").value("Theo hoat chat"));
    }

    @Test
    void queryReturnsMedicalRefusalContract() throws Exception {
        when(orchestrator.process(eq("uống bao nhiêu"), eq(null), eq(null), eq(null)))
                .thenReturn(new ChatQueryResponse(
                        "MEDICAL_REFUSAL",
                        "Gioi han an toan y te",
                        "Xin loi, chatbot khong the tu van lieu dung.",
                        "MEDICAL_UNSAFE",
                        "HIGH",
                        false,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(new ChatQueryResponse.Action("CHAT_PHARMACIST", "Chat voi duoc si")),
                        40L
                ));

        mockMvc.perform(post("/chatbot/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
                            put("message", "uống bao nhiêu");
                            put("actionId", null);
                        }})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("MEDICAL_REFUSAL"))
                .andExpect(jsonPath("$.safe").value(false))
                .andExpect(jsonPath("$.sources").isArray())
                .andExpect(jsonPath("$.suggestedActions[0].id").value("CHAT_PHARMACIST"));
    }
}
