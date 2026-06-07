package com.pharmacy.purchase_service.controller;

import com.pharmacy.purchase_service.dto.*;
import com.pharmacy.purchase_service.entity.Purchase;
import com.pharmacy.purchase_service.entity.PurchaseItem;
import com.pharmacy.purchase_service.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/purchase/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService service;

    /* =======================
       CREATE / READ
       ======================= */

    @PostMapping
    public Purchase create(@RequestBody CreatePurchaseRequest req, Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        return service.create(req, userId);
    }

    @GetMapping("/{id}")
    public Purchase get(@PathVariable Long id){
        return service.get(id);
    }

    @GetMapping("/{id}/items")
    public List<PurchaseItem> items(@PathVariable Long id){
        return service.items(id);
    }

    /* =======================
       LIST + FILTER
       ======================= */

    @GetMapping
    public List<Purchase> list(@RequestParam(required = false) String status,
                               @RequestParam(required = false) Long supplierId,
                               @RequestParam(required = false) LocalDateTime dateFrom,
                               @RequestParam(required = false) LocalDateTime dateTo){
        return service.list(status, supplierId, dateFrom, dateTo);
    }

    /* =======================
       UPDATE / CANCEL (DRAFT only)
       ======================= */

    @PutMapping("/{id}")
    public Purchase update(@PathVariable Long id,
                           @RequestBody UpdatePurchaseRequest req,
                           Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        return service.update(id, req, userId);
    }

    @PostMapping("/{id}/cancel")
    public Purchase cancel(@PathVariable Long id,
                           @RequestBody(required = false) CancelPurchaseRequest req,
                           Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        String reason = (req == null) ? null : req.reason();
        return service.cancel(id, reason, userId);
    }

    /* =======================
       ITEMS CRUD (DRAFT only)
       ======================= */

    @PostMapping("/{id}/items")
    public PurchaseItem addItem(@PathVariable Long id,
                                @RequestBody AddPurchaseItemRequest req,
                                Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        return service.addItem(id, req, userId);
    }

    @PutMapping("/{id}/items/{itemId}")
    public PurchaseItem updateItem(@PathVariable Long id,
                                   @PathVariable Long itemId,
                                   @RequestBody UpdatePurchaseItemRequest req,
                                   Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        return service.updateItem(id, itemId, req, userId);
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public void deleteItem(@PathVariable Long id,
                           @PathVariable Long itemId,
                           Authentication auth){
        Long userId = (Long) auth.getPrincipal();
        service.deleteItem(id, itemId, userId);
    }

    /* =======================
       RECEIVE (DRAFT -> RECEIVED)
       ======================= */

    @PostMapping("/{id}/receive")
    public ReceiveResponse receive(@PathVariable Long id,
                                   @RequestHeader("Authorization") String authHeader,
                                   Authentication auth){
        String token = authHeader.substring(7);
        Long userId = (Long) auth.getPrincipal();
        return service.receive(id, token, userId);
    }
}
