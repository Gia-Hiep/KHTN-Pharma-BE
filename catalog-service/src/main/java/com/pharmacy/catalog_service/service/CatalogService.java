package com.pharmacy.catalog_service.service;

import com.pharmacy.catalog_service.dto.*;
import com.pharmacy.catalog_service.entity.*;
import com.pharmacy.catalog_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CategoryRepo categoryRepo;
    private final MedicineRepo medicineRepo;
    private final MedicinePriceHistoryRepo priceHistoryRepo;
    private final MedicinePricingTierRepo pricingTierRepo;
    private final MedicineUnitRepo medicineUnitRepo;
    private final DiseaseGroupRepo diseaseGroupRepo;
    private final MedicineDiseaseGroupRepo medicineDiseaseGroupRepo;
    private final FileStorageService fileStorageService;

    private static final List<String> SEARCH_PREFIXES = List.of(
            "cho toi xem", "cho toi tim", "tim kiem", "liet ke",
            "co nhung", "san pham cho", "thuoc cho", "nhom benh",
            "trieu chung", "dau hieu", "gia thuoc", "danh muc",
            "san pham", "thuoc", "benh", "tim", "xem", "mua", "gia"
    );

    // ===== Category =====
    public List<Category> listCategories() {
        return categoryRepo.findAll();
    }

    @Transactional
    public Category createCategory(CreateCategoryRequest req) {
        Category c = new Category();
        c.setCode(req.code());
        c.setName(req.name());
        c.setDescription(req.description());
        return categoryRepo.save(c);
    }

    // ===== Medicine =====
    public List<Medicine> listMedicines() {
        return medicineRepo.findAll().stream()
                .map(this::attachActiveUnits)
                .toList();
    }

    public Medicine getMedicine(Long id) {
        return attachActiveUnits(medicineRepo.findById(id).orElseThrow());
    }

    public Medicine getByCode(String code) {
        return attachActiveUnits(medicineRepo.findByCode(code).orElseThrow());
    }

    public Medicine getByBarcode(String barcode) {
        return attachActiveUnits(medicineRepo.findByBarcode(barcode).orElseThrow());
    }

    public List<Medicine> search(String q) {
        if (q == null || q.isBlank()) {
            return medicineRepo.findAll().stream()
                    .map(this::attachActiveUnits)
                    .toList();
        }

        Map<Long, Medicine> results = new LinkedHashMap<>();
        for (String candidate : buildSearchCandidates(q)) {
            medicineRepo.search(candidate).forEach(medicine -> {
                if (medicine.getId() != null) {
                    results.putIfAbsent(medicine.getId(), attachActiveUnits(medicine));
                }
            });
        }
        return new ArrayList<>(results.values());
    }

    public List<SmartMedicineSearchResponse> smartSearchMedicines(String q, int limit) {
        if (q == null || q.isBlank()) {
            return List.of();
        }

        int resolvedLimit = Math.max(1, Math.min(limit, 20));
        List<String> candidates = buildSearchCandidates(q);
        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, Category> categoriesById = new LinkedHashMap<>();
        categoryRepo.findAll().forEach(category -> {
            if (category.getId() != null) {
                categoriesById.put(category.getId(), category);
            }
        });

        Map<Long, DiseaseGroup> diseaseGroupsById = new LinkedHashMap<>();
        diseaseGroupRepo.findAll().forEach(group -> {
            if (group.getId() != null) {
                diseaseGroupsById.put(group.getId(), group);
            }
        });

        Map<Long, List<DiseaseGroup>> diseaseGroupsByMedicine = new LinkedHashMap<>();
        medicineDiseaseGroupRepo.findAll().forEach(mapping -> {
            DiseaseGroup group = diseaseGroupsById.get(mapping.getDiseaseGroupId());
            if (group != null) {
                diseaseGroupsByMedicine
                        .computeIfAbsent(mapping.getMedicineId(), ignored -> new ArrayList<>())
                        .add(group);
            }
        });

        Map<Long, SmartMedicineSearchResponse> bestResults = new LinkedHashMap<>();
        for (Medicine medicine : medicineRepo.findAll()) {
            if (!"ACTIVE".equalsIgnoreCase(medicine.getStatus())) {
                continue;
            }

            SearchMatch bestMatch = findBestSearchMatch(
                    medicine,
                    categoriesById.get(medicine.getCategoryId()),
                    diseaseGroupsByMedicine.getOrDefault(medicine.getId(), List.of()),
                    candidates
            );
            if (bestMatch == null) {
                continue;
            }

            SmartMedicineSearchResponse response = toSmartSearchResponse(
                    medicine,
                    categoriesById.get(medicine.getCategoryId()),
                    bestMatch
            );
            bestResults.put(medicine.getId(), response);
        }

        return bestResults.values().stream()
                .sorted(Comparator
                        .comparingInt(SmartMedicineSearchResponse::score).reversed()
                        .thenComparing(SmartMedicineSearchResponse::name, String.CASE_INSENSITIVE_ORDER))
                .limit(resolvedLimit)
                .toList();
    }

    @Transactional
    public Medicine createMedicine(CreateMedicineRequest req) {
        Medicine m = new Medicine();
        m.setCode(req.code());
        m.setName(req.name());
        m.setGenericName(req.genericName());
        m.setUnit(req.unit());
        m.setRx(req.isRx() != null && req.isRx());
        m.setManufacturer(req.manufacturer());
        m.setActiveIngredient(req.activeIngredient());
        m.setDosageForm(req.dosageForm());
        m.setPackageSize(req.packageSize());
        m.setOrigin(req.origin());
        m.setCategoryId(req.categoryId());
        m.setDefaultSupplierId(req.defaultSupplierId());
        m.setSalePrice(req.salePrice() == null ? BigDecimal.ZERO : req.salePrice());
        m.setBarcode(req.barcode());
        m.setImageUrl(req.imageUrl());
        m.setDescription(req.description());
        m.setUsageInstructions(req.usageInstructions());
        m.setSideEffects(req.sideEffects());
        m.setStatus(req.status() == null ? "ACTIVE" : req.status());

        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        Medicine saved = medicineRepo.save(m);
        ensureBaseUnitSynced(saved);
        return attachActiveUnits(saved);
    }

    @Transactional
    public Medicine updateMedicine(Long id, UpdateMedicineRequest req, Long userIdForPriceHistory) {
        Medicine m = medicineRepo.findById(id).orElseThrow();
        if (req.code() != null) m.setCode(req.code());
        if (req.name() != null) m.setName(req.name());
        if (req.genericName() != null) m.setGenericName(req.genericName());
        if (req.unit() != null) m.setUnit(req.unit());
        if (req.isRx() != null) m.setRx(req.isRx());
        if (req.manufacturer() != null) m.setManufacturer(req.manufacturer());
        if (req.activeIngredient() != null) m.setActiveIngredient(req.activeIngredient());
        if (req.dosageForm() != null) m.setDosageForm(req.dosageForm());
        if (req.packageSize() != null) m.setPackageSize(req.packageSize());
        if (req.origin() != null) m.setOrigin(req.origin());
        if (req.categoryId() != null) m.setCategoryId(req.categoryId());
        if (req.defaultSupplierId() != null) m.setDefaultSupplierId(req.defaultSupplierId());
        if (req.barcode() != null) m.setBarcode(req.barcode());
        if (req.imageUrl() != null) m.setImageUrl(req.imageUrl());
        if (req.description() != null) m.setDescription(req.description());
        if (req.usageInstructions() != null) m.setUsageInstructions(req.usageInstructions());
        if (req.sideEffects() != null) m.setSideEffects(req.sideEffects());
        if (req.status() != null) m.setStatus(req.status());

        // nếu update salePrice thì ghi lịch sử
        if (req.salePrice() != null && m.getSalePrice() != null && req.salePrice().compareTo(m.getSalePrice()) != 0) {
            insertPriceHistory(m.getId(), m.getSalePrice(), req.salePrice(), userIdForPriceHistory);
            m.setSalePrice(req.salePrice());
        } else if (req.salePrice() != null && m.getSalePrice() == null) {
            insertPriceHistory(m.getId(), BigDecimal.ZERO, req.salePrice(), userIdForPriceHistory);
            m.setSalePrice(req.salePrice());
        }

        m.setUpdatedAt(LocalDateTime.now());
        Medicine saved = medicineRepo.save(m);
        ensureBaseUnitSynced(saved);
        return attachActiveUnits(saved);
    }

    @Transactional
    public Medicine updatePrice(Long id, BigDecimal newPrice, Long userId) {
        Medicine m = medicineRepo.findById(id).orElseThrow();
        BigDecimal old = m.getSalePrice() == null ? BigDecimal.ZERO : m.getSalePrice();

        if (newPrice == null) throw new RuntimeException("newPrice is required");
        if (newPrice.compareTo(old) != 0) {
            insertPriceHistory(m.getId(), old, newPrice, userId);
            m.setSalePrice(newPrice);
            m.setUpdatedAt(LocalDateTime.now());
            Medicine saved = medicineRepo.save(m);
            ensureBaseUnitSynced(saved);
            return attachActiveUnits(saved);
        }
        ensureBaseUnitSynced(m);
        return attachActiveUnits(m);
    }

    private void insertPriceHistory(Long medicineId, BigDecimal oldPrice, BigDecimal newPrice, Long userId) {
        MedicinePriceHistory h = new MedicinePriceHistory();
        h.setMedicineId(medicineId);
        h.setOldPrice(oldPrice);
        h.setNewPrice(newPrice);
        h.setChangedBy(userId);
        h.setChangedAt(LocalDateTime.now());
        priceHistoryRepo.save(h);
    }

    // ===== Medicine Image Upload =====

    @Transactional
    public UploadMedicineImageResponse uploadMedicineImage(Long medicineId, MultipartFile file) {
        Medicine m = medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));

        String fileName = fileStorageService.store(file);
        String imageUrl = "/uploads/medicines/" + fileName;

        m.setImageUrl(imageUrl);
        m.setUpdatedAt(LocalDateTime.now());
        medicineRepo.save(m);

        return new UploadMedicineImageResponse(m.getId(), imageUrl, fileName);
    }

    // ===== Pricing Tiers (Giá sỉ/lẻ/khuyến mãi) =====

    public List<MedicinePricingTier> getPricingTiers(Long medicineId) {
        return pricingTierRepo.findByMedicineId(medicineId);
    }

    public List<MedicinePricingTier> getActivePricingTiers(Long medicineId) {
        return pricingTierRepo.findAllActiveByMedicineId(medicineId, LocalDateTime.now());
    }

    @Transactional
    public MedicinePricingTier createPricingTier(CreatePricingTierRequest req, Long userId) {
        // Validate medicine exists
        medicineRepo.findById(req.medicineId())
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + req.medicineId()));

        MedicinePricingTier tier = new MedicinePricingTier();
        tier.setMedicineId(req.medicineId());
        tier.setTierCode(req.tierCode());
        tier.setMinQty(req.minQty());
        tier.setPrice(req.price());
        tier.setDiscountPercent(req.discountPercent());
        tier.setEffectiveFrom(req.effectiveFrom());
        tier.setEffectiveTo(req.effectiveTo());
        tier.setCreatedBy(userId);
        return pricingTierRepo.save(tier);
    }

    public PriceResponse calculatePrice(Long medicineId, String unitCode, String saleMode, Integer qty) {
        Medicine m = medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));

        int resolvedQty = qty != null && qty > 0 ? qty : 1;
        MedicineUnit unit = resolvePricingUnit(medicineId, unitCode)
                .orElseGet(() -> ensureBaseUnitSynced(m));

        if (unit != null) {
            boolean applyWholesale = unit.getWholesalePrice() != null
                    && unit.getWholesaleMinQty() != null
                    && resolvedQty >= unit.getWholesaleMinQty();

            BigDecimal resolvedPrice = applyWholesale
                    ? unit.getWholesalePrice()
                    : (unit.getRetailPrice() != null ? unit.getRetailPrice() : m.getSalePrice());

            String resolvedSaleMode = applyWholesale ? "WHOLESALE" : "RETAIL";
            return PriceResponse.of(
                    medicineId,
                    resolvedSaleMode,
                    resolvedSaleMode,
                    unit.getUnitCode(),
                    unit.getUnitLabel(),
                    unit.getConversionFactor(),
                    resolvedQty,
                    resolvedPrice != null ? resolvedPrice : BigDecimal.ZERO,
                    BigDecimal.ZERO
            );
        }

        // Legacy fallback if unit data is not ready yet
        String resolvedMode = saleMode != null && !saleMode.isBlank() ? saleMode : "RETAIL";
        var tierPrice = pricingTierRepo.findBestPrice(medicineId, resolvedMode, resolvedQty);
        if (tierPrice.isPresent()) {
            var t = tierPrice.get();
            return PriceResponse.of(medicineId, resolvedMode, resolvedQty, t.getPrice(), t.getDiscountPercent());
        }

        return PriceResponse.of(
                medicineId,
                resolvedMode,
                resolvedMode,
                null,
                m.getUnit(),
                1,
                resolvedQty,
                m.getSalePrice(),
                BigDecimal.ZERO
        );
    }

    @Transactional
    public void deletePricingTier(Long tierId) {
        pricingTierRepo.deleteById(tierId);
    }

    // ===== Medicine Units =====

    public List<MedicineUnit> getMedicineUnits(Long medicineId, boolean includeInactive) {
        Medicine medicine = medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));
        ensureBaseUnitSynced(medicine);
        return includeInactive
                ? medicineUnitRepo.findByMedicineIdOrdered(medicineId)
                : medicineUnitRepo.findActiveByMedicineIdOrdered(medicineId);
    }

    @Transactional
    public MedicineUnit createMedicineUnit(Long medicineId, CreateMedicineUnitRequest req, Long userId) {
        Medicine medicine = medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));

        MedicineUnit unit = new MedicineUnit();
        unit.setMedicineId(medicineId);
        applyCreateUnitRequest(unit, req, medicine);
        validateUnit(unit, medicine);
        ensureUnitCodeUnique(medicineId, unit.getUnitCode(), null);

        if (Boolean.TRUE.equals(unit.getIsBaseUnit())) {
            demoteExistingBaseUnit(medicineId);
            unit.setConversionFactor(1);
        }
        if (Boolean.TRUE.equals(unit.getIsDefaultSaleUnit())) {
            clearDefaultUnit(medicineId);
        }

        MedicineUnit saved = medicineUnitRepo.save(unit);
        syncMedicineFromBaseUnit(saved, userId);
        ensureDefaultUnitExists(medicineId);
        return saved;
    }

    @Transactional
    public MedicineUnit updateMedicineUnit(Long medicineId, Long unitId, UpdateMedicineUnitRequest req, Long userId) {
        Medicine medicine = medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));
        MedicineUnit unit = medicineUnitRepo.findById(unitId)
                .orElseThrow(() -> new RuntimeException("MedicineUnit not found: " + unitId));
        if (!Objects.equals(unit.getMedicineId(), medicineId)) {
            throw new RuntimeException("Unit does not belong to medicine: " + unitId);
        }
        if (Boolean.TRUE.equals(unit.getIsBaseUnit()) && Boolean.FALSE.equals(req.isBaseUnit())) {
            throw new RuntimeException("Base unit cannot be unset");
        }

        applyUpdateUnitRequest(unit, req, medicine);
        validateUnit(unit, medicine);
        ensureUnitCodeUnique(medicineId, unit.getUnitCode(), unitId);

        if (Boolean.TRUE.equals(unit.getIsBaseUnit())) {
            demoteExistingBaseUnit(medicineId, unitId);
            unit.setConversionFactor(1);
            unit.setIsActive(true);
        }
        if (Boolean.TRUE.equals(unit.getIsDefaultSaleUnit())) {
            clearDefaultUnit(medicineId, unitId);
            unit.setIsActive(true);
        }
        if (Boolean.FALSE.equals(unit.getIsActive()) && Boolean.TRUE.equals(unit.getIsBaseUnit())) {
            throw new RuntimeException("Base unit cannot be deactivated");
        }

        MedicineUnit saved = medicineUnitRepo.save(unit);
        syncMedicineFromBaseUnit(saved, userId);
        ensureDefaultUnitExists(medicineId);
        return saved;
    }

    @Transactional
    public void deactivateMedicineUnit(Long medicineId, Long unitId, Long userId) {
        medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));
        MedicineUnit unit = medicineUnitRepo.findById(unitId)
                .orElseThrow(() -> new RuntimeException("MedicineUnit not found: " + unitId));
        if (!Objects.equals(unit.getMedicineId(), medicineId)) {
            throw new RuntimeException("Unit does not belong to medicine: " + unitId);
        }
        if (Boolean.TRUE.equals(unit.getIsBaseUnit())) {
            throw new RuntimeException("Base unit cannot be deactivated");
        }
        unit.setIsActive(false);
        unit.setIsDefaultSaleUnit(false);
        medicineUnitRepo.save(unit);
        ensureDefaultUnitExists(medicineId);
    }

    // ===== Disease Groups (Nhóm bệnh) =====

    public List<DiseaseGroup> listDiseaseGroups() {
        return diseaseGroupRepo.findAll();
    }

    public DiseaseGroup getDiseaseGroup(Long id) {
        return diseaseGroupRepo.findById(id).orElseThrow();
    }

    public List<DiseaseGroup> searchDiseaseGroups(String q) {
        if (q == null || q.isBlank()) {
            return diseaseGroupRepo.findAll();
        }

        Map<Long, DiseaseGroup> results = new LinkedHashMap<>();
        for (String candidate : buildSearchCandidates(q)) {
            diseaseGroupRepo.search(candidate).forEach(group -> {
                if (group.getId() != null) {
                    results.putIfAbsent(group.getId(), group);
                }
            });
        }
        return new ArrayList<>(results.values());
    }

    @Transactional
    public DiseaseGroup createDiseaseGroup(CreateDiseaseGroupRequest req) {
        if (diseaseGroupRepo.findByCode(req.code()).isPresent()) {
            throw new RuntimeException("DiseaseGroup code already exists: " + req.code());
        }

        DiseaseGroup dg = new DiseaseGroup();
        dg.setCode(req.code());
        dg.setName(req.name());
        dg.setDescription(req.description());
        dg.setKeywords(req.keywords());
        return diseaseGroupRepo.save(dg);
    }

    // ===== Medicine - Disease Group Mapping =====

    public List<Long> getDiseaseGroupIdsForMedicine(Long medicineId) {
        return medicineDiseaseGroupRepo.findByMedicineId(medicineId)
                .stream()
                .map(MedicineDiseaseGroup::getDiseaseGroupId)
                .toList();
    }

    @Transactional
    public void assignDiseaseGroups(Long medicineId, List<Long> diseaseGroupIds) {
        // Validate medicine exists
        medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));

        // Remove old mappings
        medicineDiseaseGroupRepo.deleteByMedicineId(medicineId);

        // Add new mappings
        for (Long dgId : diseaseGroupIds) {
            if (diseaseGroupRepo.findById(dgId).isEmpty()) {
                throw new RuntimeException("DiseaseGroup not found: " + dgId);
            }
            MedicineDiseaseGroup mdg = new MedicineDiseaseGroup();
            mdg.setMedicineId(medicineId);
            mdg.setDiseaseGroupId(dgId);
            medicineDiseaseGroupRepo.save(mdg);
        }
    }

    public List<Medicine> findMedicinesByDiseaseGroup(Long diseaseGroupId) {
        List<Long> medicineIds = medicineDiseaseGroupRepo.findMedicineIdsByDiseaseGroupId(diseaseGroupId);
        if (medicineIds.isEmpty()) {
            return List.of();
        }
        return medicineRepo.findAllById(medicineIds).stream()
                .map(this::attachActiveUnits)
                .toList();
    }

    private Medicine attachActiveUnits(Medicine medicine) {
        if (medicine == null || medicine.getId() == null) return medicine;
        ensureBaseUnitSynced(medicine);
        medicine.setUnits(medicineUnitRepo.findActiveByMedicineIdOrdered(medicine.getId()));
        return medicine;
    }

    private Optional<MedicineUnit> resolvePricingUnit(Long medicineId, String unitCode) {
        if (unitCode != null && !unitCode.isBlank()) {
            return medicineUnitRepo.findByMedicineIdAndUnitCode(medicineId, unitCode)
                    .filter(unit -> Boolean.TRUE.equals(unit.getIsActive()));
        }
        return medicineUnitRepo.findActiveByMedicineIdOrdered(medicineId).stream()
                .filter(unit -> Boolean.TRUE.equals(unit.getIsDefaultSaleUnit()))
                .findFirst()
                .or(() -> medicineUnitRepo.findByMedicineIdAndIsBaseUnitTrue(medicineId)
                        .filter(unit -> Boolean.TRUE.equals(unit.getIsActive())));
    }

    private MedicineUnit ensureBaseUnitSynced(Medicine medicine) {
        MedicineUnit baseUnit = medicineUnitRepo.findByMedicineIdAndIsBaseUnitTrue(medicine.getId())
                .orElseGet(() -> {
                    MedicineUnit created = new MedicineUnit();
                    created.setMedicineId(medicine.getId());
                    created.setUnitCode(normalizeUnitCode(medicine.getUnit()));
                    created.setIsBaseUnit(true);
                    created.setIsDefaultSaleUnit(true);
                    return created;
                });

        baseUnit.setMedicineId(medicine.getId());
        String targetBaseCode = normalizeUnitCode(medicine.getUnit());
        if (baseUnit.getUnitCode() == null
                || baseUnit.getUnitCode().isBlank()
                || "BASE".equalsIgnoreCase(baseUnit.getUnitCode())) {
            MedicineUnit canonicalUnit = medicineUnitRepo.findByMedicineIdAndUnitCode(medicine.getId(), targetBaseCode)
                    .filter(existing -> baseUnit.getId() == null || !Objects.equals(existing.getId(), baseUnit.getId()))
                    .orElse(null);

            if (canonicalUnit != null) {
                canonicalUnit.setUnitLabel(medicine.getUnit());
                canonicalUnit.setConversionFactor(1);
                canonicalUnit.setRetailPrice(medicine.getSalePrice() == null ? BigDecimal.ZERO : medicine.getSalePrice());
                canonicalUnit.setIsBaseUnit(true);
                canonicalUnit.setIsDefaultSaleUnit(true);
                canonicalUnit.setIsActive(true);
                MedicineUnit savedCanonical = medicineUnitRepo.save(canonicalUnit);

                if (baseUnit.getId() != null) {
                    medicineUnitRepo.delete(baseUnit);
                    medicineUnitRepo.flush();
                }

                ensureDefaultUnitExists(medicine.getId());
                return savedCanonical;
            }

            baseUnit.setUnitCode(targetBaseCode);
        }
        baseUnit.setUnitLabel(medicine.getUnit());
        baseUnit.setConversionFactor(1);
        baseUnit.setRetailPrice(medicine.getSalePrice() == null ? BigDecimal.ZERO : medicine.getSalePrice());
        baseUnit.setIsBaseUnit(true);
        baseUnit.setIsActive(true);
        if (baseUnit.getIsDefaultSaleUnit() == null) {
            baseUnit.setIsDefaultSaleUnit(true);
        }

        MedicineUnit saved = medicineUnitRepo.save(baseUnit);
        ensureDefaultUnitExists(medicine.getId());
        return saved;
    }

    private void syncMedicineFromBaseUnit(MedicineUnit baseUnit, Long userIdForPriceHistory) {
        if (baseUnit == null || !Boolean.TRUE.equals(baseUnit.getIsBaseUnit())) return;

        Medicine medicine = medicineRepo.findById(baseUnit.getMedicineId())
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + baseUnit.getMedicineId()));

        boolean changed = false;
        if (baseUnit.getUnitLabel() != null && !baseUnit.getUnitLabel().equals(medicine.getUnit())) {
            medicine.setUnit(baseUnit.getUnitLabel());
            changed = true;
        }

        BigDecimal nextPrice = baseUnit.getRetailPrice() == null ? BigDecimal.ZERO : baseUnit.getRetailPrice();
        BigDecimal currentPrice = medicine.getSalePrice() == null ? BigDecimal.ZERO : medicine.getSalePrice();
        if (nextPrice.compareTo(currentPrice) != 0) {
            if (userIdForPriceHistory != null) {
                insertPriceHistory(medicine.getId(), currentPrice, nextPrice, userIdForPriceHistory);
            }
            medicine.setSalePrice(nextPrice);
            changed = true;
        }

        if (changed) {
            medicine.setUpdatedAt(LocalDateTime.now());
            medicineRepo.save(medicine);
        }
    }

    private void ensureDefaultUnitExists(Long medicineId) {
        List<MedicineUnit> activeUnits = medicineUnitRepo.findActiveByMedicineIdOrdered(medicineId);
        boolean hasDefault = activeUnits.stream().anyMatch(unit -> Boolean.TRUE.equals(unit.getIsDefaultSaleUnit()));
        if (!hasDefault) {
            activeUnits.stream().findFirst().ifPresent(unit -> {
                unit.setIsDefaultSaleUnit(true);
                medicineUnitRepo.save(unit);
            });
        }
    }

    private void clearDefaultUnit(Long medicineId) {
        clearDefaultUnit(medicineId, null);
    }

    private void clearDefaultUnit(Long medicineId, Long exceptUnitId) {
        medicineUnitRepo.findByMedicineIdOrdered(medicineId).stream()
                .filter(unit -> exceptUnitId == null || !Objects.equals(unit.getId(), exceptUnitId))
                .filter(unit -> Boolean.TRUE.equals(unit.getIsDefaultSaleUnit()))
                .forEach(unit -> {
                    unit.setIsDefaultSaleUnit(false);
                    medicineUnitRepo.save(unit);
                });
    }

    private void demoteExistingBaseUnit(Long medicineId) {
        demoteExistingBaseUnit(medicineId, null);
    }

    private void demoteExistingBaseUnit(Long medicineId, Long exceptUnitId) {
        medicineUnitRepo.findByMedicineIdOrdered(medicineId).stream()
                .filter(unit -> Boolean.TRUE.equals(unit.getIsBaseUnit()))
                .filter(unit -> exceptUnitId == null || !Objects.equals(unit.getId(), exceptUnitId))
                .forEach(unit -> {
                    unit.setIsBaseUnit(false);
                    medicineUnitRepo.save(unit);
                });
    }

    private void applyCreateUnitRequest(MedicineUnit unit, CreateMedicineUnitRequest req, Medicine medicine) {
        unit.setUnitCode(resolveUnitCode(req.unitCode(), req.unitLabel()));
        unit.setUnitLabel(req.unitLabel() != null && !req.unitLabel().isBlank() ? req.unitLabel().trim() : medicine.getUnit());
        unit.setConversionFactor(req.conversionFactor() != null ? req.conversionFactor() : 1);
        unit.setRetailPrice(req.retailPrice() != null ? req.retailPrice() : medicine.getSalePrice());
        unit.setWholesalePrice(req.wholesalePrice());
        unit.setWholesaleMinQty(req.wholesaleMinQty());
        unit.setIsBaseUnit(Boolean.TRUE.equals(req.isBaseUnit()));
        unit.setIsDefaultSaleUnit(Boolean.TRUE.equals(req.isDefaultSaleUnit()) || Boolean.TRUE.equals(req.isBaseUnit()));
        unit.setIsActive(req.isActive() == null || req.isActive());
    }

    private void applyUpdateUnitRequest(MedicineUnit unit, UpdateMedicineUnitRequest req, Medicine medicine) {
        if (req.unitCode() != null && !req.unitCode().isBlank()) {
            unit.setUnitCode(resolveUnitCode(req.unitCode(), req.unitLabel() != null ? req.unitLabel() : unit.getUnitLabel()));
        }
        if (req.unitLabel() != null && !req.unitLabel().isBlank()) {
            unit.setUnitLabel(req.unitLabel().trim());
            if (unit.getUnitCode() == null || unit.getUnitCode().isBlank() || "BASE".equalsIgnoreCase(unit.getUnitCode())) {
                unit.setUnitCode(resolveUnitCode(unit.getUnitCode(), unit.getUnitLabel()));
            }
        }
        if (req.conversionFactor() != null) {
            unit.setConversionFactor(req.conversionFactor());
        }
        if (req.retailPrice() != null) {
            unit.setRetailPrice(req.retailPrice());
        }
        if (req.wholesalePrice() != null || (req.wholesalePrice() == null && req.wholesaleMinQty() != null)) {
            unit.setWholesalePrice(req.wholesalePrice());
        }
        if (req.wholesaleMinQty() != null) {
            unit.setWholesaleMinQty(req.wholesaleMinQty());
        }
        if (req.isBaseUnit() != null) {
            unit.setIsBaseUnit(req.isBaseUnit());
        }
        if (req.isDefaultSaleUnit() != null) {
            unit.setIsDefaultSaleUnit(req.isDefaultSaleUnit());
        }
        if (req.isActive() != null) {
            unit.setIsActive(req.isActive());
        }
        if (unit.getUnitLabel() == null || unit.getUnitLabel().isBlank()) {
            unit.setUnitLabel(medicine.getUnit());
        }
        if (unit.getRetailPrice() == null) {
            unit.setRetailPrice(medicine.getSalePrice());
        }
    }

    private void validateUnit(MedicineUnit unit, Medicine medicine) {
        if (unit.getUnitLabel() == null || unit.getUnitLabel().isBlank()) {
            throw new RuntimeException("Tên đơn vị không được để trống");
        }
        if (unit.getUnitCode() == null || unit.getUnitCode().isBlank()) {
            unit.setUnitCode(normalizeUnitCode(unit.getUnitLabel()));
        }
        if (unit.getConversionFactor() == null || unit.getConversionFactor() < 1) {
            throw new RuntimeException("Hệ số quy đổi phải lớn hơn hoặc bằng 1");
        }
        if (unit.getRetailPrice() == null || unit.getRetailPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Giá bán lẻ phải lớn hơn hoặc bằng 0");
        }
        if (unit.getWholesalePrice() != null && unit.getWholesalePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Giá bán sỉ phải lớn hơn hoặc bằng 0");
        }
        if (unit.getWholesalePrice() != null && (unit.getWholesaleMinQty() == null || unit.getWholesaleMinQty() < 1)) {
            unit.setWholesaleMinQty(1);
        }
        if (Boolean.TRUE.equals(unit.getIsBaseUnit())) {
            unit.setConversionFactor(1);
            unit.setUnitLabel(unit.getUnitLabel() != null ? unit.getUnitLabel() : medicine.getUnit());
            unit.setRetailPrice(unit.getRetailPrice() != null ? unit.getRetailPrice() : medicine.getSalePrice());
        }
    }

    private void ensureUnitCodeUnique(Long medicineId, String unitCode, Long exceptUnitId) {
        medicineUnitRepo.findByMedicineIdAndUnitCode(medicineId, unitCode)
                .filter(existing -> exceptUnitId == null || !Objects.equals(existing.getId(), exceptUnitId))
                .ifPresent(existing -> {
                    throw new RuntimeException("Mã đơn vị '" + unitCode + "' đã tồn tại cho thuốc này");
                });
    }

    private SmartMedicineSearchResponse toSmartSearchResponse(
            Medicine medicine,
            Category category,
            SearchMatch match
    ) {
        return new SmartMedicineSearchResponse(
                medicine.getId(),
                medicine.getCode(),
                medicine.getName(),
                medicine.getGenericName(),
                medicine.getSalePrice(),
                medicine.getActiveIngredient(),
                medicine.getDescription(),
                medicine.getUsageInstructions(),
                medicine.getSideEffects(),
                medicine.getCategoryId(),
                category == null ? null : category.getName(),
                medicine.getImageUrl(),
                medicine.getStatus(),
                match.matchType(),
                match.matchLabel(),
                match.matchedKeyword(),
                match.score()
        );
    }

    private SearchMatch findBestSearchMatch(
            Medicine medicine,
            Category category,
            List<DiseaseGroup> diseaseGroups,
            List<String> candidates
    ) {
        SearchMatch best = null;
        for (String candidate : candidates) {
            best = betterMatch(best, matchField(medicine.getName(), candidate, "NAME", "Theo ten thuoc", 95));
            best = betterMatch(best, matchField(medicine.getGenericName(), candidate, "GENERIC_NAME", "Theo ten hoat chat/generic", 86));
            best = betterMatch(best, matchField(medicine.getActiveIngredient(), candidate, "ACTIVE_INGREDIENT", "Theo hoat chat", 84));
            best = betterMatch(best, matchField(medicine.getCode(), candidate, "CODE", "Theo ma thuoc", 78));
            best = betterMatch(best, matchField(medicine.getDescription(), candidate, "DESCRIPTION", "Theo mo ta san pham", 58));
            best = betterMatch(best, matchField(medicine.getDosageForm(), candidate, "DOSAGE_FORM", "Theo dang bao che", 54));
            best = betterMatch(best, matchField(medicine.getPackageSize(), candidate, "PACKAGE_SIZE", "Theo quy cach dong goi", 50));
            best = betterMatch(best, matchField(medicine.getManufacturer(), candidate, "MANUFACTURER", "Theo nha san xuat", 48));

            if (category != null) {
                String categoryText = String.join(" ",
                        safeText(category.getName()),
                        safeText(category.getCode()),
                        safeText(category.getDescription())
                );
                best = betterMatch(best, matchField(
                        categoryText,
                        candidate,
                        "CATEGORY",
                        "Theo danh muc: " + category.getName(),
                        76
                ));
            }

            for (DiseaseGroup group : diseaseGroups) {
                String groupText = String.join(" ",
                        safeText(group.getName()),
                        safeText(group.getCode()),
                        safeText(group.getDescription()),
                        safeText(group.getKeywords())
                );
                best = betterMatch(best, matchField(
                        groupText,
                        candidate,
                        "DISEASE_GROUP",
                        "Theo nhom benh: " + group.getName(),
                        82
                ));
            }
        }
        return best;
    }

    private SearchMatch matchField(
            String fieldValue,
            String candidate,
            String matchType,
            String matchLabel,
            int baseScore
    ) {
        String normalizedField = normalizeSearchText(fieldValue);
        String normalizedCandidate = normalizeSearchText(candidate);
        if (normalizedField.isBlank() || normalizedCandidate.isBlank()) {
            return null;
        }

        if (normalizedField.equals(normalizedCandidate)) {
            return new SearchMatch(matchType, matchLabel, candidate, baseScore + 10);
        }
        if (normalizedField.contains(normalizedCandidate)) {
            return new SearchMatch(matchType, matchLabel, candidate, baseScore);
        }
        if (normalizedCandidate.length() >= 4 && normalizedCandidate.contains(normalizedField)) {
            return new SearchMatch(matchType, matchLabel, fieldValue, baseScore - 10);
        }
        return null;
    }

    private SearchMatch betterMatch(SearchMatch current, SearchMatch candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || candidate.score() > current.score()) {
            return candidate;
        }
        return current;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private List<String> buildSearchCandidates(String rawQuery) {
        String normalized = normalizeSearchText(rawQuery);
        if (normalized.isBlank()) {
            return List.of("");
        }

        List<String> candidates = new ArrayList<>();
        addSearchCandidate(candidates, normalized);
        addSearchCandidate(candidates, stripLeadingSearchPhrases(normalized));
        return candidates;
    }

    private void addSearchCandidate(List<String> candidates, String value) {
        String normalized = normalizeSearchText(value);
        if (!normalized.isBlank() && !candidates.contains(normalized)) {
            candidates.add(normalized);
        }
    }

    private String stripLeadingSearchPhrases(String query) {
        String result = normalizeSearchText(query);
        boolean changed;
        do {
            changed = false;
            for (String prefix : SEARCH_PREFIXES) {
                String normalizedPrefix = normalizeSearchText(prefix);
                if (result.startsWith(normalizedPrefix + " ")) {
                    result = result.substring(normalizedPrefix.length()).trim();
                    changed = true;
                    break;
                }
            }
        } while (changed && !result.isBlank());

        return result.isBlank() ? query : result;
    }

    private String normalizeSearchText(String text) {
        if (text == null) {
            return "";
        }
        return Normalizer.normalize(text.toLowerCase(Locale.ROOT).trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('\u0111', 'd')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private record SearchMatch(String matchType, String matchLabel, String matchedKeyword, int score) {}

    private String normalizeUnitCode(String value) {
        String source = value == null || value.isBlank() ? "BASE" : value;
        String normalized = Normalizer.normalize(source, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z0-9]+", "_")
                .replaceAll("^_+|_+$", "")
                .toUpperCase(Locale.ROOT);
        return normalized.isBlank() ? "BASE" : normalized;
    }

    private String resolveUnitCode(String requestedUnitCode, String unitLabel) {
        String normalizedRequested = requestedUnitCode != null && !requestedUnitCode.isBlank()
                ? requestedUnitCode.trim().toUpperCase(Locale.ROOT)
                : "";
        if (!normalizedRequested.isBlank() && !"BASE".equals(normalizedRequested)) {
            return normalizedRequested;
        }

        String normalizedFromLabel = normalizeUnitCode(unitLabel);
        if (!"BASE".equals(normalizedFromLabel)) {
            return normalizedFromLabel;
        }

        return "BASE";
    }
}
