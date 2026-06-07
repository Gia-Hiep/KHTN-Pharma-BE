package com.pharmacy.catalog_service.controller;

import com.pharmacy.catalog_service.dto.CreatePricingTierRequest;
import com.pharmacy.catalog_service.dto.PriceResponse;
import com.pharmacy.catalog_service.entity.MedicinePricingTier;
import com.pharmacy.catalog_service.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final CatalogService service;

    /**
     * Lấy tất cả pricing tiers của medicine (bao gồm cả expired)
     */
    @GetMapping("/medicine/{medicineId}")
    public List<MedicinePricingTier> getTiers(@PathVariable Long medicineId) {
        return service.getPricingTiers(medicineId);
    }

    /**
     * Lấy các pricing tiers đang có hiệu lực
     */
    @GetMapping("/medicine/{medicineId}/active")
    public List<MedicinePricingTier> getActiveTiers(@PathVariable Long medicineId) {
        return service.getActivePricingTiers(medicineId);
    }

    /**
     * Tính giá cho medicine dựa trên tier và số lượng
     */
    @GetMapping("/calculate")
    public PriceResponse calculatePrice(
            @RequestParam Long medicineId,
            @RequestParam(required = false) String unitCode,
            @RequestParam(defaultValue = "RETAIL") String tierCode,
            @RequestParam(required = false) String saleMode,
            @RequestParam(defaultValue = "1") Integer qty
    ) {
        return service.calculatePrice(medicineId, unitCode, saleMode != null ? saleMode : tierCode, qty);
    }

    /**
     * Tạo pricing tier mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public MedicinePricingTier create(
            @Valid @RequestBody CreatePricingTierRequest req,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        return service.createPricingTier(req, userId);
    }

    /**
     * Xóa pricing tier
     */
    @DeleteMapping("/{tierId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long tierId) {
        service.deletePricingTier(tierId);
    }
}
