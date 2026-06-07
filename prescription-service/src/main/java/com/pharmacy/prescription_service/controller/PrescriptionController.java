package com.pharmacy.prescription_service.controller;

import com.pharmacy.prescription_service.dto.*;
import com.pharmacy.prescription_service.entity.*;
import com.pharmacy.prescription_service.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService service;

    @PostMapping
    public Prescription create(@RequestBody CreatePrescriptionRequest req,
                               Authentication auth,
                               @RequestHeader("Authorization") String authHeader){
        Long userId = (Long) auth.getPrincipal();
        String token = authHeader.substring(7);
        return service.create(req, userId, token);
    }

    @PostMapping("/{id}/items")
    public PrescriptionItem addItem(@PathVariable Long id, @RequestBody AddPrescriptionItemRequest req){
        return service.addItem(id, req);
    }

    @GetMapping("/{id}")
    public Prescription get(@PathVariable Long id){
        return service.get(id);
    }

    @GetMapping("/{id}/items")
    public List<PrescriptionItem> items(@PathVariable Long id){
        return service.items(id);
    }

    @PostMapping("/{id}/status")
    public Prescription updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest req){
        return service.updateStatus(id, req.status());
    }
    /**
     * GET /prescriptions
     * filter optional:
     * - q: search text (code/phone/doctor)
     * - status: PENDING/PROCESSING/COMPLETED/CANCELLED
     * - phone: exact customer phone
     * - dateFrom, dateTo: prescription_date range
     * - createdBy: userId created_by
     */
    @GetMapping
    public List<Prescription> list(@RequestParam(required = false) String q,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam(required = false) LocalDate dateFrom,
                                   @RequestParam(required = false) LocalDate dateTo,
                                   @RequestParam(required = false) Long createdBy){
        return service.list(q, status, phone, dateFrom, dateTo, createdBy);
    }
}
