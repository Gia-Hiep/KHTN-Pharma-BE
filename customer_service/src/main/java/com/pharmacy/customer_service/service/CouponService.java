package com.pharmacy.customer_service.service;

import com.pharmacy.customer_service.dto.ApplyCouponRequest;
import com.pharmacy.customer_service.dto.ApplyCouponResponse;
import com.pharmacy.customer_service.dto.CreateCouponRequest;
import com.pharmacy.customer_service.entity.Coupon;
import com.pharmacy.customer_service.entity.CouponUsage;
import com.pharmacy.customer_service.entity.Customer;
import com.pharmacy.customer_service.repository.CouponRepo;
import com.pharmacy.customer_service.repository.CouponUsageRepo;
import com.pharmacy.customer_service.repository.CustomerRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepo couponRepo;
    private final CouponUsageRepo usageRepo;
    private final CustomerRepo customerRepo;

    public List<Coupon> listActiveCoupons() {
        return couponRepo.findByIsActiveTrue();
    }

    public Coupon getCoupon(Long id) {
        return couponRepo.findById(id).orElseThrow();
    }

    public Coupon getByCode(String code) {
        return couponRepo.findByCode(code).orElseThrow();
    }

    @Transactional
    public Coupon createCoupon(CreateCouponRequest req, Long userId) {
        if (couponRepo.findByCode(req.code()).isPresent()) {
            throw new RuntimeException("Coupon code already exists: " + req.code());
        }

        Coupon c = new Coupon();
        c.setCode(req.code().toUpperCase());
        c.setDiscountType(req.discountType());
        c.setDiscountValue(req.discountValue());
        c.setMinOrderAmount(req.minOrderAmount());
        c.setMaxDiscount(req.maxDiscount());
        c.setUsageLimit(req.usageLimit());
        c.setValidFrom(req.validFrom());
        c.setValidTo(req.validTo());
        c.setCustomerTier(req.customerTier());
        c.setCustomerType(req.customerType());
        c.setDescription(req.description());
        c.setCreatedBy(userId);

        return couponRepo.save(c);
    }

    /**
     * Kiểm tra và áp dụng coupon
     */
    @Transactional
    public ApplyCouponResponse applyCoupon(ApplyCouponRequest req) {
        // Tìm coupon
        Coupon coupon = couponRepo.findByCode(req.couponCode().toUpperCase()).orElse(null);
        if (coupon == null) {
            return ApplyCouponResponse.invalid("Coupon not found");
        }

        // Kiểm tra hiệu lực
        if (!coupon.isValid()) {
            return ApplyCouponResponse.invalid("Coupon is expired or usage limit reached");
        }

        // Kiểm tra customer
        Customer customer = customerRepo.findById(req.customerId()).orElse(null);
        if (customer == null) {
            return ApplyCouponResponse.invalid("Customer not found");
        }

        // Kiểm tra tier và type
        if (coupon.getCustomerTier() != null && !coupon.getCustomerTier().equals(customer.getTier())) {
            return ApplyCouponResponse.invalid("Coupon not valid for your tier");
        }
        if (coupon.getCustomerType() != null && !coupon.getCustomerType().equals(customer.getCustomerType())) {
            return ApplyCouponResponse.invalid("Coupon not valid for your customer type");
        }

        // Tính discount
        BigDecimal discount = coupon.calculateDiscount(req.orderAmount());
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return ApplyCouponResponse.invalid("Order amount does not meet minimum requirement: " + coupon.getMinOrderAmount());
        }

        // Lưu usage
        CouponUsage usage = new CouponUsage();
        usage.setCouponId(coupon.getId());
        usage.setCustomerId(req.customerId());
        usage.setInvoiceId(req.invoiceId());
        usage.setDiscountApplied(discount);
        usageRepo.save(usage);

        // Tăng used count
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepo.save(coupon);

        return ApplyCouponResponse.success(coupon.getCode(), coupon.getDiscountType(), discount);
    }

    /**
     * Validate coupon mà không apply
     */
    public ApplyCouponResponse validateCoupon(String code, Long customerId, BigDecimal orderAmount) {
        Coupon coupon = couponRepo.findByCode(code.toUpperCase()).orElse(null);
        if (coupon == null) {
            return ApplyCouponResponse.invalid("Coupon not found");
        }

        if (!coupon.isValid()) {
            return ApplyCouponResponse.invalid("Coupon is expired or usage limit reached");
        }

        Customer customer = customerRepo.findById(customerId).orElse(null);
        if (customer == null) {
            return ApplyCouponResponse.invalid("Customer not found");
        }

        if (coupon.getCustomerTier() != null && !coupon.getCustomerTier().equals(customer.getTier())) {
            return ApplyCouponResponse.invalid("Coupon not valid for your tier");
        }

        BigDecimal discount = coupon.calculateDiscount(orderAmount);
        if (discount.compareTo(BigDecimal.ZERO) == 0) {
            return ApplyCouponResponse.invalid("Order amount does not meet minimum requirement");
        }

        return ApplyCouponResponse.success(coupon.getCode(), coupon.getDiscountType(), discount);
    }

    @Transactional
    public void deactivateCoupon(Long couponId) {
        Coupon coupon = couponRepo.findById(couponId).orElseThrow();
        coupon.setIsActive(false);
        couponRepo.save(coupon);
    }
}
