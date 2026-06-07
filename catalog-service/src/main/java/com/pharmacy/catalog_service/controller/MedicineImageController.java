package com.pharmacy.catalog_service.controller;

import com.pharmacy.catalog_service.dto.MedicineImageResponse;
import com.pharmacy.catalog_service.dto.ReorderImagesRequest;
import com.pharmacy.catalog_service.service.MedicineImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/catalog/medicines/{medicineId}/images")
@RequiredArgsConstructor
public class MedicineImageController {

    private final MedicineImageService imageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public List<MedicineImageResponse> upload(
            @PathVariable Long medicineId,
            @RequestParam("files") MultipartFile[] files) {
        return imageService.uploadImages(medicineId, files);
    }

    @GetMapping
    public List<MedicineImageResponse> list(@PathVariable Long medicineId) {
        return imageService.listImages(medicineId);
    }

    @PutMapping("/{imageId}/primary")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public MedicineImageResponse setPrimary(
            @PathVariable Long medicineId,
            @PathVariable Long imageId) {
        return imageService.setPrimary(medicineId, imageId);
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public List<MedicineImageResponse> reorder(
            @PathVariable Long medicineId,
            @RequestBody ReorderImagesRequest req) {
        return imageService.reorderImages(medicineId, req.imageIds());
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public void delete(
            @PathVariable Long medicineId,
            @PathVariable Long imageId) {
        imageService.deleteImage(medicineId, imageId);
    }
}
