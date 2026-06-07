package com.pharmacy.catalog_service.controller;

import com.pharmacy.catalog_service.dto.AssignDiseaseGroupsRequest;
import com.pharmacy.catalog_service.dto.CreateDiseaseGroupRequest;
import com.pharmacy.catalog_service.entity.DiseaseGroup;
import com.pharmacy.catalog_service.entity.Medicine;
import com.pharmacy.catalog_service.service.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog/disease-groups")
@RequiredArgsConstructor
public class DiseaseGroupController {

    private final CatalogService service;

    /**
     * Danh sách tất cả nhóm bệnh
     */
    @GetMapping
    public List<DiseaseGroup> list() {
        return service.listDiseaseGroups();
    }

    /**
     * Chi tiết nhóm bệnh
     */
    @GetMapping("/{id}")
    public DiseaseGroup get(@PathVariable Long id) {
        return service.getDiseaseGroup(id);
    }

    /**
     * Tìm kiếm nhóm bệnh theo tên hoặc keywords
     */
    @GetMapping("/search")
    public List<DiseaseGroup> search(@RequestParam String q) {
        return service.searchDiseaseGroups(q);
    }

    /**
     * Tạo nhóm bệnh mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public DiseaseGroup create(@Valid @RequestBody CreateDiseaseGroupRequest req) {
        return service.createDiseaseGroup(req);
    }

    /**
     * Lấy danh sách thuốc thuộc nhóm bệnh
     * (Dùng cho chatbot tìm kiếm thuốc theo triệu chứng)
     */
    @GetMapping("/{id}/medicines")
    public List<Medicine> getMedicines(@PathVariable Long id) {
        return service.findMedicinesByDiseaseGroup(id);
    }

    /**
     * Lấy danh sách nhóm bệnh của medicine
     */
    @GetMapping("/medicine/{medicineId}")
    public List<Long> getMedicineDiseaseGroups(@PathVariable Long medicineId) {
        return service.getDiseaseGroupIdsForMedicine(medicineId);
    }

    /**
     * Gán nhóm bệnh cho medicine
     */
    @PutMapping("/medicine/{medicineId}")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public void assignDiseaseGroups(
            @PathVariable Long medicineId,
            @Valid @RequestBody AssignDiseaseGroupsRequest req
    ) {
        service.assignDiseaseGroups(medicineId, req.diseaseGroupIds());
    }
}
