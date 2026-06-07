package com.pharmacy.chatbot_service.service;

import com.pharmacy.chatbot_service.dto.ChatRequest;
import com.pharmacy.chatbot_service.dto.ChatResponse;
import com.pharmacy.chatbot_service.dto.CreateFaqRequest;
import com.pharmacy.chatbot_service.dto.FeedbackRequest;
import com.pharmacy.chatbot_service.dto.SessionResponse;
import com.pharmacy.chatbot_service.entity.BotFaq;
import com.pharmacy.chatbot_service.entity.BotFeedback;
import com.pharmacy.chatbot_service.entity.BotMessage;
import com.pharmacy.chatbot_service.entity.BotSession;
import com.pharmacy.chatbot_service.repository.BotFaqRepo;
import com.pharmacy.chatbot_service.repository.BotFeedbackRepo;
import com.pharmacy.chatbot_service.repository.BotMessageRepo;
import com.pharmacy.chatbot_service.repository.BotSessionRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final BotFaqRepo faqRepo;
    private final BotSessionRepo sessionRepo;
    private final BotMessageRepo messageRepo;
    private final BotFeedbackRepo feedbackRepo;
    private final IntentRecognizer intentRecognizer;
    private final GuardrailService guardrailService;

    private static final int MAX_FALLBACK = 3;

    /**
     * Legacy guardrail for the deprecated FAQ chatbot.
     * Kept only for backward compatibility on /chatbot/message.
     */
    private static final List<String> SENSITIVE_KEYWORDS = List.of(
            "ke don", "viet don", "can don thuoc",
            "lieu dung cho toi", "toi nen dung bao nhieu",
            "uong bao nhieu vien", "uong may vien", "dung bao nhieu mg",
            "chan doan", "toi bi benh gi",
            "don thuoc cho", "ke cho toi",
            "lieu cho tre em", "lieu cho ba bau", "lieu cho phu nu mang thai",
            "thay the bac si", "khong can kham"
    );

    private static final String GUARDRAIL_REPLY =
            "Xin loi, toi khong the tu van lieu dung ca nhan, ke don thuoc hoac chan doan benh.\n\n" +
            "Ban nen:\n" +
            "- Lien he duoc si tai nha thuoc\n" +
            "- Gap bac si de duoc kham va ke don\n" +
            "- Hoac chat voi nhan vien ho tro de duoc tu van them\n\n" +
            "Toi co the giup ban tra cuu thong tin chung ve thuoc, theo doi don hang, hoac tra loi FAQ.";

    private static final String DISCLAIMER =
            "\n\nThong tin chi mang tinh tham khao, khong thay the tu van duoc si hoac bac si.";

    @PostConstruct
    @Transactional
    public void cleanupUnsafeLegacyFaqs() {
        List<BotFaq> faqs = faqRepo.findAll();
        boolean changed = false;

        for (BotFaq faq : faqs) {
            if (Boolean.TRUE.equals(faq.getActive()) && shouldDisableLegacyFaq(faq)) {
                faq.setActive(false);
                changed = true;
                log.warn("Disabled unsafe legacy FAQ id={} intent={} question={}",
                        faq.getId(), faq.getIntent(), faq.getQuestion());
            }
        }

        if (changed) {
            faqRepo.saveAll(faqs);
        }
    }

    @Deprecated(since = "phase-1-cleanup")
    @Transactional
    public ChatResponse chat(ChatRequest req, Long userId) {
        log.warn("Deprecated legacy chatbot endpoint '/chatbot/message' was called. sessionToken={}, userId={}",
                req.sessionToken(), userId);

        BotSession session = getOrCreateSession(userId, req.sessionToken());
        session.setLastActiveAt(LocalDateTime.now());

        String normalizedInput = intentRecognizer.normalize(req.message());
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (normalizedInput.contains(intentRecognizer.normalize(keyword))) {
                saveMessage(session.getId(), "USER", req.message(), "GUARDRAIL", 1.0, null);
                ChatResponse safeResponse = new ChatResponse(
                        session.getSessionToken(),
                        "GUARDRAIL",
                        GUARDRAIL_REPLY,
                        1.0,
                        "TEXT",
                        List.of("Tra cuu thuoc", "Don hang", "Lien he nhan vien"),
                        null
                );
                saveMessage(session.getId(), "BOT", GUARDRAIL_REPLY, "GUARDRAIL", 1.0, null);
                sessionRepo.save(session);
                return safeResponse;
            }
        }

        IntentRecognizer.IntentResult result = intentRecognizer.recognize(req.message());

        saveMessage(
                session.getId(),
                "USER",
                req.message(),
                result.intent(),
                result.confidence(),
                result.matchedFaq() != null ? result.matchedFaq().getId() : null
        );

        ChatResponse response;
        if ("FALLBACK".equals(result.intent())) {
            session.setFallbackCount(session.getFallbackCount() + 1);
            if (session.getFallbackCount() >= MAX_FALLBACK) {
                session.setStatus("ESCALATED");
                response = buildEscalateResponse(session.getSessionToken());
            } else {
                response = buildFallbackResponse(session.getSessionToken(), session.getFallbackCount());
            }
        } else {
            session.setFallbackCount(0);
            session.setCurrentIntent(result.intent());
            response = buildIntentResponse(session.getSessionToken(), result);
        }

        sessionRepo.save(session);
        saveMessage(session.getId(), "BOT", response.reply(), result.intent(), result.confidence(), null);
        return response;
    }

    @Transactional(readOnly = true)
    public SessionResponse getSessionById(Long sessionId) {
        BotSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session khong ton tai"));
        List<BotMessage> messages = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return SessionResponse.from(session, messages);
    }

    @Transactional(readOnly = true)
    public List<BotMessage> getHistory(String sessionToken, Long userId) {
        BotSession session = findSession(sessionToken, userId);
        return messageRepo.findBySessionIdOrderByCreatedAtAsc(session.getId());
    }

    @Transactional
    public void endSession(String sessionToken, Long userId) {
        BotSession session = findSession(sessionToken, userId);
        session.setStatus("ENDED");
        sessionRepo.save(session);
    }

    @Transactional(readOnly = true)
    public List<BotFaq> getAllFaqs() {
        return faqRepo.findAllActive();
    }

    @Transactional(readOnly = true)
    public List<BotFaq> searchFaqs(String query) {
        String normalized = intentRecognizer.normalize(query);
        return faqRepo.findAllActive().stream()
                .filter(faq ->
                        intentRecognizer.normalize(faq.getQuestion()).contains(normalized)
                                || intentRecognizer.normalize(faq.getKeywords()).contains(normalized)
                                || intentRecognizer.normalize(faq.getIntent()).contains(normalized))
                .toList();
    }

    @Transactional
    public BotFaq createFaq(CreateFaqRequest req) {
        validateFaqRequest(req);

        BotFaq faq = new BotFaq();
        faq.setIntent(req.intent());
        faq.setKeywords(req.keywords());
        faq.setQuestion(req.question());
        faq.setAnswer(req.answer());
        faq.setPriority(req.priority() != null ? req.priority() : 5);
        return faqRepo.save(faq);
    }

    @Transactional
    public BotFaq updateFaq(Long id, CreateFaqRequest req) {
        validateFaqRequest(req);

        BotFaq faq = faqRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FAQ khong ton tai"));
        faq.setIntent(req.intent());
        faq.setKeywords(req.keywords());
        faq.setQuestion(req.question());
        faq.setAnswer(req.answer());
        if (req.priority() != null) {
            faq.setPriority(req.priority());
        }
        return faqRepo.save(faq);
    }

    @Transactional
    public void deleteFaq(Long id) {
        BotFaq faq = faqRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "FAQ khong ton tai"));
        faq.setActive(false);
        faqRepo.save(faq);
    }

    @Transactional
    public BotFeedback saveFeedback(FeedbackRequest req, Long userId) {
        sessionRepo.findById(req.sessionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session khong ton tai"));

        BotFeedback fb = new BotFeedback();
        fb.setSessionId(req.sessionId());
        fb.setUserId(userId);
        fb.setRating(req.rating());
        fb.setComment(req.comment());
        fb.setResolved(req.resolved());
        return feedbackRepo.save(fb);
    }

    private BotSession getOrCreateSession(Long userId, String sessionToken) {
        if (userId != null) {
            return sessionRepo.findByUserIdAndStatus(userId, "ACTIVE")
                    .orElseGet(() -> createNewSession(userId, null));
        }
        if (sessionToken != null && !sessionToken.isBlank()) {
            return sessionRepo.findBySessionTokenAndStatus(sessionToken, "ACTIVE")
                    .orElseGet(() -> createNewSession(null, sessionToken));
        }
        return createNewSession(null, UUID.randomUUID().toString());
    }

    private BotSession createNewSession(Long userId, String sessionToken) {
        BotSession session = new BotSession();
        session.setUserId(userId);
        session.setSessionToken(sessionToken != null ? sessionToken : UUID.randomUUID().toString());
        return sessionRepo.save(session);
    }

    private BotSession findSession(String sessionToken, Long userId) {
        if (userId != null) {
            return sessionRepo.findByUserIdAndStatus(userId, "ACTIVE")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay session"));
        }
        return sessionRepo.findBySessionTokenAndStatus(sessionToken, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay session"));
    }

    private void saveMessage(Long sessionId, String role, String content,
                             String intent, double confidence, Long faqId) {
        BotMessage msg = new BotMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setIntent(intent);
        msg.setConfidence(confidence);
        msg.setFaqId(faqId);
        messageRepo.save(msg);
    }

    private ChatResponse buildIntentResponse(String sessionToken, IntentRecognizer.IntentResult result) {
        BotFaq faq = result.matchedFaq();
        return new ChatResponse(
                sessionToken,
                result.intent(),
                faq.getAnswer() + DISCLAIMER,
                result.confidence(),
                "TEXT",
                null,
                getSuggestions(result.intent())
        );
    }

    private ChatResponse buildFallbackResponse(String sessionToken, int fallbackCount) {
        String reply = switch (fallbackCount) {
            case 1 -> "Xin loi, minh chua hieu cau hoi cua ban. Ban co the dien dat lai khong?\n\n"
                    + "Ban co the hoi ve:\n"
                    + "- Tim kiem thuoc\n"
                    + "- Don hang\n"
                    + "- Chinh sach giao hang\n"
                    + "- Lien he ho tro";
            case 2 -> "Minh van chua hieu ro y ban. Hay thu hoi cu the hon.";
            default -> "Minh dang gap kho khan trong viec hieu yeu cau cua ban.";
        };

        return new ChatResponse(
                sessionToken,
                "FALLBACK",
                reply + DISCLAIMER,
                0.0,
                "OPTIONS",
                List.of("Tim thuoc", "Tra cuu don hang", "Xem chinh sach", "Noi chuyen voi nhan vien"),
                null
        );
    }

    private ChatResponse buildEscalateResponse(String sessionToken) {
        return new ChatResponse(
                sessionToken,
                "ESCALATE",
                "Xin loi vi su bat tien. Minh se ket noi ban voi nhan vien tu van ngay." + DISCLAIMER,
                0.0,
                "ESCALATE",
                null,
                null
        );
    }

    private List<String> getSuggestions(String intent) {
        return switch (intent) {
            case "MEDICINE_SEARCH" -> List.of("Xem chi tiet thuoc", "Gia thuoc", "Con hang khong?");
            case "ORDER_STATUS" -> List.of("Xem don hang", "Huy don", "Lien he ho tro");
            default -> List.of("Tim thuoc", "Hoi duoc si", "Xem chinh sach");
        };
    }

    private boolean shouldDisableLegacyFaq(BotFaq faq) {
        GuardrailService.GuardrailResult result = guardrailService.checkFaqContent(
                faq.getIntent(),
                faq.getKeywords(),
                faq.getQuestion(),
                faq.getAnswer()
        );
        return !result.safe();
    }

    private void validateFaqRequest(CreateFaqRequest req) {
        GuardrailService.GuardrailResult result = guardrailService.checkFaqContent(
                req.intent(),
                req.keywords(),
                req.question(),
                req.answer()
        );
        if (!result.safe()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "FAQ y te ca nhan khong duoc phep trong chatbot"
            );
        }
    }
}
