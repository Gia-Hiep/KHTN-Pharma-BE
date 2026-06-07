package com.pharmacy.sales_service.service;

import com.pharmacy.sales_service.client.CatalogClient;
import com.pharmacy.sales_service.dto.AddCartItemRequest;
import com.pharmacy.sales_service.dto.CartResponse;
import com.pharmacy.sales_service.dto.CartResponse.CartItemResponse;
import com.pharmacy.sales_service.dto.UpdateCartItemRequest;
import com.pharmacy.sales_service.entity.Cart;
import com.pharmacy.sales_service.entity.CartItem;
import com.pharmacy.sales_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepo;
    private final CatalogClient catalogClient;

    /* ── Lấy giỏ hàng ── */
    @Transactional
    public CartResponse getCart(Long buyerId, String bearerToken) {
        Cart cart = cartRepo.findByBuyerId(buyerId).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            return new CartResponse(null, buyerId, List.of(), 0, BigDecimal.ZERO);
        }
        boolean changed = false;
        for (CartItem item : cart.getItems()) {
            changed |= refreshPricing(item, bearerToken);
        }
        if (changed) {
            cartRepo.save(cart);
        }
        return toResponse(cart);
    }

    /* ── Thêm / cộng dồn sản phẩm ── */
    @Transactional
    public CartResponse addItem(Long buyerId, AddCartItemRequest req, String bearerToken) {
        Cart cart = cartRepo.findByBuyerId(buyerId)
                .orElseGet(() -> cartRepo.save(Cart.builder().buyerId(buyerId).build()));

        // Tìm item trùng (medicineId + saleMode + unitCode)
        String tier = resolveSaleMode(req.priceTier(), req.saleMode());
        Integer conversionFactor = resolveConversionFactor(req.conversionFactor());
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getMedicineId().equals(req.medicineId())
                        && tier.equals(i.getPriceTier())
                        && Objects.equals(req.unitCode(), i.getUnitCode()))
                .findFirst().orElse(null);

        if (existing != null) {
            existing.setQty(existing.getQty() + (req.qty() != null ? req.qty() : 1));
            // Cập nhật giá mới nhất
            if (req.unitPrice() != null) existing.setUnitPrice(req.unitPrice());
            if (req.medicineName() != null) existing.setMedicineName(req.medicineName());
            if (req.imageUrl() != null) existing.setImageUrl(req.imageUrl());
            if (req.unitCode() != null) existing.setUnitCode(req.unitCode());
            if (req.unitLabel() != null) existing.setUnitLabel(req.unitLabel());
            existing.setConversionFactor(conversionFactor);
            existing.setPriceTier(tier);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .medicineId(req.medicineId())
                    .medicineName(req.medicineName())
                    .imageUrl(req.imageUrl())
                    .qty(req.qty() != null ? req.qty() : 1)
                    .unitCode(req.unitCode())
                    .unitLabel(req.unitLabel())
                    .conversionFactor(conversionFactor)
                    .unitPrice(req.unitPrice())
                    .priceTier(tier)
                    .build();
            cart.getItems().add(item);
            existing = item;
        }

        refreshPricing(existing, bearerToken);
        cartRepo.save(cart);
        return toResponse(cart);
    }

    /* ── Cập nhật số lượng ── */
    @Transactional
    public CartResponse updateItemQty(Long buyerId, Long itemId, UpdateCartItemRequest req, String bearerToken) {
        Cart cart = getCartOrThrow(buyerId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + itemId));

        if (req.qty() == null || req.qty() < 1) {
            throw new RuntimeException("Số lượng phải >= 1");
        }
        item.setQty(req.qty());
        refreshPricing(item, bearerToken);
        cartRepo.save(cart);
        return toResponse(cart);
    }

    /* ── Xóa 1 item ── */
    @Transactional
    public CartResponse removeItem(Long buyerId, Long itemId) {
        Cart cart = getCartOrThrow(buyerId);
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) {
            throw new RuntimeException("Cart item not found: " + itemId);
        }
        cartRepo.save(cart);
        return toResponse(cart);
    }

    /* ── Xóa toàn bộ giỏ hàng ── */
    @Transactional
    public void clearCart(Long buyerId) {
        cartRepo.findByBuyerId(buyerId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepo.save(cart);
        });
    }

    /* ── Helpers ── */

    private Cart getCartOrThrow(Long buyerId) {
        return cartRepo.findByBuyerId(buyerId)
                .orElseThrow(() -> new RuntimeException("Giỏ hàng trống"));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getId(),
                        i.getMedicineId(),
                        i.getMedicineName(),
                        i.getImageUrl(),
                        i.getQty(),
                        i.getUnitCode(),
                        i.getUnitLabel(),
                        i.getConversionFactor(),
                        i.getUnitPrice(),
                        i.getPriceTier(),
                        i.getPriceTier(),
                        i.getUnitPrice() != null
                                ? i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQty()))
                                : BigDecimal.ZERO
                ))
                .toList();

        int totalQty = items.stream().mapToInt(CartItemResponse::qty).sum();
        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), cart.getBuyerId(), items, totalQty, totalAmount);
    }

    private String resolveSaleMode(String priceTier, String saleMode) {
        if (saleMode != null && !saleMode.isBlank()) return saleMode;
        if (priceTier != null && !priceTier.isBlank()) return priceTier;
        return "RETAIL";
    }

    private Integer resolveConversionFactor(Integer conversionFactor) {
        return conversionFactor != null && conversionFactor > 0 ? conversionFactor : 1;
    }

    private boolean refreshPricing(CartItem item, String bearerToken) {
        String currentSaleMode = resolveSaleMode(item.getPriceTier(), null);
        var priceResult = catalogClient.calculatePrice(
                bearerToken,
                item.getMedicineId(),
                item.getUnitCode(),
                currentSaleMode,
                item.getQty() != null && item.getQty() > 0 ? item.getQty() : 1
        );

        if (priceResult == null) {
            if (item.getConversionFactor() == null || item.getConversionFactor() < 1) {
                item.setConversionFactor(1);
                return true;
            }
            return false;
        }

        boolean changed = false;
        BigDecimal nextPrice = priceResult.unitPrice() != null ? priceResult.unitPrice() : item.getUnitPrice();
        if (!Objects.equals(item.getUnitPrice(), nextPrice)) {
            item.setUnitPrice(nextPrice);
            changed = true;
        }

        String nextSaleMode = resolveSaleMode(priceResult.tierCode(), priceResult.saleMode());
        if (!Objects.equals(item.getPriceTier(), nextSaleMode)) {
            item.setPriceTier(nextSaleMode);
            changed = true;
        }

        if (priceResult.unitCode() != null && !Objects.equals(item.getUnitCode(), priceResult.unitCode())) {
            item.setUnitCode(priceResult.unitCode());
            changed = true;
        }
        if (priceResult.unitLabel() != null && !Objects.equals(item.getUnitLabel(), priceResult.unitLabel())) {
            item.setUnitLabel(priceResult.unitLabel());
            changed = true;
        }

        Integer nextConversionFactor = resolveConversionFactor(priceResult.conversionFactor());
        if (!Objects.equals(item.getConversionFactor(), nextConversionFactor)) {
            item.setConversionFactor(nextConversionFactor);
            changed = true;
        }

        return changed;
    }
}
