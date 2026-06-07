package com.pharmacy.prescription_service.service;

import com.pharmacy.prescription_service.client.CustomerClient;
import com.pharmacy.prescription_service.dto.*;
import com.pharmacy.prescription_service.entity.*;
import com.pharmacy.prescription_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepo prescriptionRepo;
    private final PrescriptionItemRepo itemRepo;
    private final CustomerClient customerClient;

    @Transactional
    public Prescription create(CreatePrescriptionRequest req, Long userId, String bearerToken){

        Long customerId = null;

        if (req.customerPhone() != null && !req.customerPhone().isBlank()) {
            var c = customerClient.getByPhone(bearerToken, req.customerPhone());

            if (c == null) {
                String name = (req.customerName() == null || req.customerName().isBlank())
                        ? "khach le"
                        : req.customerName();

                c = customerClient.create(
                        bearerToken,
                        new CustomerClient.CreateCustomerDto(
                                name,
                                req.customerPhone(),
                                null, null, null,
                                "created from prescription-service"
                        )
                );
            }
            customerId = c.id();
        }

        Prescription p = new Prescription();
        p.setPrescriptionCode(req.prescriptionCode());
        p.setCustomerId(customerId);
        p.setCustomerPhone(req.customerPhone());
        p.setDoctorName(req.doctorName());
        p.setDiagnosis(req.diagnosis());
        p.setPrescriptionDate(req.prescriptionDate());
        p.setNotes(req.notes());
        p.setStatus("PENDING");
        p.setCreatedBy(userId);
        p.setCreatedAt(LocalDateTime.now());
        return prescriptionRepo.save(p);
    }

    @Transactional
    public PrescriptionItem addItem(Long prescriptionId, AddPrescriptionItemRequest req){
        PrescriptionItem it = new PrescriptionItem();
        it.setPrescriptionId(prescriptionId);
        it.setMedicineId(req.medicineId());
        it.setQuantity(req.quantity());
        it.setDosageInstructions(req.dosageInstructions());
        return itemRepo.save(it);
    }

    @Transactional(readOnly = true)
    public Prescription get(Long id){
        return prescriptionRepo.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionItem> items(Long id){
        return itemRepo.findByPrescriptionId(id);
    }

    @Transactional
    public Prescription updateStatus(Long id, String status){
        Prescription p = prescriptionRepo.findById(id).orElseThrow();
        p.setStatus(status);
        return prescriptionRepo.save(p);
    }


    @Transactional(readOnly = true)
    public List<Prescription> list(String q, String status, String phone,
                                   LocalDate dateFrom, LocalDate dateTo, Long createdBy){
        return prescriptionRepo.search(q, status, phone, dateFrom, dateTo, createdBy);
    }
}
