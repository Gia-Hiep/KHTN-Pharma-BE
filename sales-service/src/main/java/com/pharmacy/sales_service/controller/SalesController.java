package com.pharmacy.sales_service.controller;

import com.pharmacy.sales_service.dto.*;
import com.pharmacy.sales_service.entity.Invoice;
import com.pharmacy.sales_service.entity.InvoiceItem;
import com.pharmacy.sales_service.entity.Payment;
import com.pharmacy.sales_service.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService service;

    /* =======================
       CREATE / ADD / CHECKOUT / PAY
       ======================= */

    @PostMapping("/invoices")
    public Invoice create(@RequestBody CreateInvoiceRequest req,
                          Authentication auth,
                          @RequestHeader("Authorization") String authHeader){
        Long userId = (Long) auth.getPrincipal();
        String token = authHeader.substring(7);

        return service.createInvoice(req.code(), userId, token, req.customerPhone(), req.customerName(), req.customerId());
    }

    @PostMapping("/invoices/{invoiceId}/items")
    public InvoiceItem addItem(@PathVariable Long invoiceId,
                               @RequestBody AddItemRequest req,
                               @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        return service.addItem(invoiceId, req, token);
    }

    @DeleteMapping("/invoices/{invoiceId}/items/{itemId}")
    public void removeItem(@PathVariable Long invoiceId, @PathVariable Long itemId){
        service.removeItem(invoiceId, itemId);
    }

    @PutMapping("/invoices/{invoiceId}/items/{itemId}")
    public InvoiceItem updateItemQty(@PathVariable Long invoiceId,
                                     @PathVariable Long itemId,
                                     @RequestBody UpdateItemRequest req,
                                     @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        return service.updateItemQty(invoiceId, itemId, req.qty(), token);
    }

    @PostMapping("/invoices/{invoiceId}/checkout")
    public CheckoutResponse checkout(@PathVariable Long invoiceId,
                                     @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        return service.checkout(invoiceId, token);
    }

    @PostMapping("/invoices/{invoiceId}/pay")
    public void pay(@PathVariable Long invoiceId,
                    @RequestBody PayRequest req,
                    @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        service.pay(invoiceId, req, token);
    }

    /** Alias: FE gọi POST /invoices/{id}/payments */
    @PostMapping("/invoices/{invoiceId}/payments")
    public void addPayment(@PathVariable Long invoiceId,
                           @RequestBody PayRequest req,
                           @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        service.pay(invoiceId, req, token);
    }

    /* =======================
       READ FOR FE (GET invoices/items/payments)
       ======================= */

    // list invoices with filters
    @GetMapping("/invoices")
    public List<Invoice> listInvoices(@RequestParam(required = false) String status,
                                      @RequestParam(required = false) String paymentStatus,
                                      @RequestParam(required = false) Long cashierId,
                                      @RequestParam(required = false) String dateFrom,
                                      @RequestParam(required = false) String dateTo) {

        LocalDateTime from = parseDateTimeFlexible(dateFrom);
        LocalDateTime to = parseDateTimeFlexible(dateTo);

        return service.listInvoices(status, paymentStatus, cashierId, from, to);
    }

    @GetMapping("/invoices/{invoiceId}")
    public Invoice getInvoice(@PathVariable Long invoiceId){
        return service.getInvoice(invoiceId);
    }

    @GetMapping("/invoices/{invoiceId}/items")
    public List<InvoiceItem> getItems(@PathVariable Long invoiceId){
        return service.getItems(invoiceId);
    }

    @GetMapping("/invoices/{invoiceId}/payments")
    public List<Payment> getPayments(@PathVariable Long invoiceId){
        return service.getPayments(invoiceId);
    }

    /* =======================
       CANCEL + RELEASE (if reserved)
       ======================= */

    @PostMapping("/invoices/{invoiceId}/cancel")
    public Invoice cancel(@PathVariable Long invoiceId,
                          @RequestBody(required = false) CancelInvoiceRequest req,
                          Authentication auth,
                          @RequestHeader("Authorization") String authHeader){
        Long userId = (Long) auth.getPrincipal();
        String token = authHeader.substring(7);
        String reason = (req == null) ? null : req.reason();
        return service.cancelInvoice(invoiceId, token, userId, reason);
    }



    private LocalDateTime parseDateTimeFlexible(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            // case 1: có Z hoặc offset => parse Instant
            if (s.endsWith("Z") || s.contains("+") || s.matches(".*-\\d{2}:\\d{2}$")) {
                Instant ins = Instant.parse(s);
                return LocalDateTime.ofInstant(ins, ZoneId.systemDefault());
            }
            // case 2: không timezone => LocalDateTime
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Sai định dạng dateFrom/dateTo. Ví dụ đúng: 2026-01-09T00:00:00Z hoặc 2026-01-09T00:00:00"
            );
        }
    }
}
