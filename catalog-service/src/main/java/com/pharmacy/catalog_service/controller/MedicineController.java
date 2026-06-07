package com.pharmacy.catalog_service.controller;

import com.pharmacy.catalog_service.dto.*;
import com.pharmacy.catalog_service.entity.Medicine;
import com.pharmacy.catalog_service.entity.MedicineUnit;
import com.pharmacy.catalog_service.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/catalog/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final CatalogService service;

    @GetMapping
    public List<Medicine> list(@RequestParam(required = false) Integer page,
                               @RequestParam(required = false) Integer size) {
        return service.listMedicines();
    }

    @GetMapping("/{id}")
    public Medicine get(@PathVariable Long id) { return service.getMedicine(id); }

    @GetMapping("/code/{code}")
    public Medicine getByCode(@PathVariable String code) { return service.getByCode(code); }

    @GetMapping("/barcode/{barcode}")
    public Medicine getByBarcode(@PathVariable String barcode) { return service.getByBarcode(barcode); }

    @GetMapping("/search")
    public List<Medicine> search(@RequestParam String q) { return service.search(q); }

    @GetMapping("/smart-search")
    public List<SmartMedicineSearchResponse> smartSearch(@RequestParam String q,
                                                         @RequestParam(defaultValue = "10") int limit) {
        return service.smartSearchMedicines(q, limit);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public Medicine create(@RequestBody CreateMedicineRequest req) { return service.createMedicine(req); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public Medicine update(@PathVariable Long id, @RequestBody UpdateMedicineRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return service.updateMedicine(id, req, userId);
    }

    @PutMapping("/{id}/price")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public Medicine updatePrice(@PathVariable Long id, @RequestBody UpdatePriceRequest req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return service.updatePrice(id, req.newPrice(), userId);
    }

    @GetMapping("/{id}/units")
    public List<MedicineUnit> getUnits(@PathVariable Long id,
                                       @RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.getMedicineUnits(id, includeInactive);
    }

    @PostMapping("/{id}/units")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public MedicineUnit createUnit(@PathVariable Long id,
                                   @RequestBody CreateMedicineUnitRequest req,
                                   Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return service.createMedicineUnit(id, req, userId);
    }

    @PutMapping("/{id}/units/{unitId}")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public MedicineUnit updateUnit(@PathVariable Long id,
                                   @PathVariable Long unitId,
                                   @RequestBody UpdateMedicineUnitRequest req,
                                   Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return service.updateMedicineUnit(id, unitId, req, userId);
    }

    @DeleteMapping("/{id}/units/{unitId}")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public void deactivateUnit(@PathVariable Long id,
                               @PathVariable Long unitId,
                               Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        service.deactivateMedicineUnit(id, unitId, userId);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public UploadMedicineImageResponse uploadImage(@PathVariable Long id,
                                                    @RequestParam("file") MultipartFile file) {
        return service.uploadMedicineImage(id, file);
    }
}
