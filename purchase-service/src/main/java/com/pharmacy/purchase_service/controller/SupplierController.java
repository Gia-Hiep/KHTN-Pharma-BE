package com.pharmacy.purchase_service.controller;

import com.pharmacy.purchase_service.dto.CreateSupplierRequest;
import com.pharmacy.purchase_service.entity.Supplier;
import com.pharmacy.purchase_service.repository.SupplierRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/purchase/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierRepo supplierRepo;

    @PostMapping
    public Supplier create(@RequestBody CreateSupplierRequest req){
        Supplier s = new Supplier();
        s.setCode(req.code());
        s.setName(req.name());
        s.setContactPerson(req.contactPerson());
        s.setPhone(req.phone());
        s.setEmail(req.email());
        s.setAddress(req.address());
        s.setNotes(req.notes());
        s.setCreatedAt(LocalDateTime.now());
        return supplierRepo.save(s);
    }

    @GetMapping
    public List<Supplier> list(){
        return supplierRepo.findAll();
    }

    @PutMapping("/{id}")
    public Supplier update(@PathVariable Long id, @RequestBody CreateSupplierRequest req){
        Supplier s = supplierRepo.findById(id).orElseThrow(() -> new RuntimeException("Supplier not found"));
        if (req.name() != null) s.setName(req.name());
        if (req.phone() != null) s.setPhone(req.phone());
        if (req.email() != null) s.setEmail(req.email());
        if (req.address() != null) s.setAddress(req.address());
        if (req.contactPerson() != null) s.setContactPerson(req.contactPerson());
        if (req.notes() != null) s.setNotes(req.notes());
        return supplierRepo.save(s);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        Supplier s = supplierRepo.findById(id).orElseThrow(() -> new RuntimeException("Supplier not found"));
        supplierRepo.delete(s);
    }
}
