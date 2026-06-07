package com.pharmacy.chatbot_service.service;

import com.pharmacy.chatbot_service.entity.BotDocument;
import com.pharmacy.chatbot_service.repository.BotDocumentChunkRepo;
import com.pharmacy.chatbot_service.repository.BotDocumentRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 3 corpus bootstrap:
 * - upsert a small curated document set for thesis/demo
 * - disable legacy/outdated/unsafe documents
 * - refresh embedding cache after corpus changes
 *
 * Reindex is still manual. If content changes, old chunks are removed so the
 * chatbot never answers from stale or corrupted document chunks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseBootstrapService {

    private final BotDocumentRepo documentRepo;
    private final BotDocumentChunkRepo chunkRepo;
    private final GuardrailService guardrailService;
    private final RagService ragService;

    private static final List<StandardDocument> STANDARD_DOCUMENTS = List.of(
            new StandardDocument(
                    "Chinh sach giao hang",
                    "POLICY",
                    """
                    Chinh sach giao hang cua Pharmacy Online:
                    - Don hang duoc xu ly sau khi he thong xac nhan thanh cong.
                    - Thoi gian giao hang phu thuoc khu vuc nhan hang va trang thai xu ly don.
                    - Phi van chuyen duoc hien thi ro tai buoc thanh toan.
                    - Nguoi mua co the theo doi trang thai don trong muc Don hang.
                    """
            ),
            new StandardDocument(
                    "Chinh sach doi tra",
                    "POLICY",
                    """
                    Chinh sach doi tra:
                    - Chi ho tro doi tra theo dieu kien duoc cong bo tren he thong.
                    - San pham phai con nguyen tinh trang, con chung tu can thiet neu co.
                    - Cac mat hang duoc phap luat hoac quy dinh duoc pham gioi han doi tra se ap dung theo quy dinh hien hanh.
                    - Nguoi mua can lien he ho tro de duoc huong dan quy trinh doi tra.
                    """
            ),
            new StandardDocument(
                    "Chinh sach thanh toan",
                    "POLICY",
                    """
                    Hinh thuc thanh toan duoc ho tro:
                    - Thanh toan khi nhan hang neu don hang du dieu kien.
                    - Chuyen khoan ngan hang.
                    - Thanh toan online neu he thong dang ho tro.
                    - Hoa don va thong tin thanh toan duoc luu cung don hang de doi chieu.
                    """
            ),
            new StandardDocument(
                    "Huong dan dat hang",
                    "GUIDE",
                    """
                    Huong dan dat hang:
                    - Dang nhap tai khoan.
                    - Tim san pham theo ten, nhom benh, danh muc hoac hoat chat neu co du lieu.
                    - Them san pham vao gio hang.
                    - Kiem tra gio hang, dia chi giao hang va hinh thuc thanh toan.
                    - Xac nhan don va theo doi trang thai trong muc Don hang.
                    """
            ),
            new StandardDocument(
                    "Huong dan tra cuu don hang",
                    "GUIDE",
                    """
                    Huong dan tra cuu don hang:
                    - Dang nhap tai khoan da dat hang.
                    - Mo muc Don hang.
                    - Xem danh sach don va trang thai hien tai.
                    - Mo chi tiet tung don de xem san pham, tong tien va lich su xu ly neu he thong cung cap.
                    - Co the tim kiem theo ma don neu giao dien ho tro.
                    """
            ),
            new StandardDocument(
                    "Gio lam viec va lien he",
                    "FAQ",
                    """
                    Thong tin lien he:
                    - Gio ho tro duoc cong bo tren website.
                    - Hotline va email ho tro duoc hien thi tai khu vuc lien he.
                    - Neu can tu van chuyen mon, nguoi dung duoc huong dan chat voi duoc si.
                    """
            ),
            new StandardDocument(
                    "Gioi thieu he thong",
                    "FAQ",
                    """
                    Gioi thieu Pharmacy Online:
                    - He thong ho tro ban le va ban si duoc pham, thuc pham chuc nang va thiet bi y te.
                    - Ho tro tim san pham, dat hang, theo doi don va chat voi duoc si.
                    - Du lieu giao dich thoi gian thuc duoc tra cuu qua API he thong, khong dua vao kho tri thuc tinh.
                    """
            )
    );

    private static final List<String> LEGACY_BLOCK_MARKERS = List.of(
            "loyalty",
            "vip",
            "gold",
            "silver",
            "than thiet",
            "customer tier"
    );

    @PostConstruct
    @Transactional
    public void bootstrap() {
        List<BotDocument> existingDocs = documentRepo.findAll();
        Map<String, BotDocument> docsByTitle = new LinkedHashMap<>();
        for (BotDocument doc : existingDocs) {
            docsByTitle.putIfAbsent(normalizeTitle(doc.getTitle()), doc);
        }

        int inserted = 0;
        int updated = 0;
        int disabled = 0;
        int chunkResets = 0;

        for (StandardDocument standardDocument : STANDARD_DOCUMENTS) {
            BotDocument existing = docsByTitle.get(normalizeTitle(standardDocument.title()));
            if (existing == null) {
                BotDocument created = new BotDocument();
                created.setTitle(standardDocument.title());
                created.setSourceType(standardDocument.sourceType());
                created.setContent(standardDocument.content());
                created.setActive(true);
                documentRepo.save(created);
                inserted++;
                continue;
            }

            boolean changed = false;
            if (!standardDocument.sourceType().equalsIgnoreCase(existing.getSourceType())) {
                existing.setSourceType(standardDocument.sourceType());
                changed = true;
            }
            if (!standardDocument.content().equals(existing.getContent())) {
                existing.setContent(standardDocument.content());
                changed = true;
            }
            if (!Boolean.TRUE.equals(existing.getActive())) {
                existing.setActive(true);
                changed = true;
            }

            if (changed) {
                documentRepo.save(existing);
                chunkRepo.deleteByDocumentId(existing.getId());
                updated++;
                chunkResets++;
            }
        }

        for (BotDocument doc : documentRepo.findAll()) {
            if (!shouldDisable(doc)) {
                continue;
            }
            if (!Boolean.TRUE.equals(doc.getActive())) {
                continue;
            }
            doc.setActive(false);
            documentRepo.save(doc);
            disabled++;
        }

        ragService.loadEmbeddingsToCache();

        if (inserted > 0 || updated > 0 || disabled > 0) {
            log.info(
                    "Phase 3 knowledge base bootstrap completed: inserted={}, updated={}, disabled={}, chunkResets={}. Reindex is recommended.",
                    inserted,
                    updated,
                    disabled,
                    chunkResets
            );
        }
    }

    private boolean shouldDisable(BotDocument doc) {
        if (doc == null) {
            return false;
        }
        String title = doc.getTitle() == null ? "" : doc.getTitle();
        String content = doc.getContent() == null ? "" : doc.getContent();
        String combined = (title + "\n" + content).toLowerCase();

        if (looksCorrupted(title) || looksCorrupted(content)) {
            return true;
        }
        if (LEGACY_BLOCK_MARKERS.stream().anyMatch(combined::contains)) {
            return true;
        }

        GuardrailService.GuardrailResult safety = guardrailService.checkDocumentContent(
                doc.getSourceType(),
                doc.getTitle(),
                doc.getContent()
        );
        return !safety.safe();
    }

    private boolean looksCorrupted(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        return text.contains("\u0102")
                || text.contains("\u00C4")
                || text.contains("\u00E1\u00BB");
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.trim().toLowerCase();
    }

    private record StandardDocument(String title, String sourceType, String content) {}
}
