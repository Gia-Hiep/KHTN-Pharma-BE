package com.pharmacy.chatbot_service.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryUnderstandingServiceTest {

    @Mock
    private GeminiService geminiService;

    @Test
    void parsesGeminiJsonAndNormalizesProductQuery() {
        QueryUnderstandingService service = new QueryUnderstandingService(geminiService);
        when(geminiService.generate(any(), any())).thenReturn("""
                ```json
                {
                  "intent": "PRODUCT_SEARCH",
                  "searchMode": "DISEASE_GROUP",
                  "normalizedQuery": "cảm cúm",
                  "entities": ["cảm cúm"],
                  "needsPharmacist": false
                }
                ```
                """);

        QueryUnderstandingService.QueryUnderstanding result =
                service.understand("bệnh cảm cúm", "PRODUCT_SEARCH", "cam cum");

        assertThat(result.intent()).isEqualTo("PRODUCT_SEARCH");
        assertThat(result.searchMode()).isEqualTo("DISEASE_GROUP");
        assertThat(result.normalizedQuery()).isEqualTo("cam cum");
        assertThat(result.entities()).isEqualTo(List.of("cam cum"));
        assertThat(result.source()).isEqualTo("GEMINI");
    }

    @Test
    void fallsBackToRuleWhenGeminiFails() {
        QueryUnderstandingService service = new QueryUnderstandingService(geminiService);
        when(geminiService.generate(any(), any())).thenReturn(null);

        QueryUnderstandingService.QueryUnderstanding result =
                service.understand("paracetamol", "PRODUCT_SEARCH", "paracetamol");

        assertThat(result.intent()).isEqualTo("PRODUCT_SEARCH");
        assertThat(result.normalizedQuery()).isEqualTo("paracetamol");
        assertThat(result.source()).isEqualTo("RULE");
    }

    @Test
    void fallsBackToRuleWhenIntentIsInvalid() {
        QueryUnderstandingService service = new QueryUnderstandingService(geminiService);
        when(geminiService.generate(any(), any())).thenReturn("""
                {"intent":"BUY_MEDICINE_NOW","searchMode":"GENERAL","normalizedQuery":"cam cum","entities":[],"needsPharmacist":false}
                """);

        QueryUnderstandingService.QueryUnderstanding result =
                service.understand("cam cum", "PRODUCT_SEARCH", "cam cum");

        assertThat(result.intent()).isEqualTo("PRODUCT_SEARCH");
        assertThat(result.source()).isEqualTo("RULE");
    }
}
