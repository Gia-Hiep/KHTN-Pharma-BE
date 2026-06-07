package com.pharmacy.catalog_service.service;

import com.pharmacy.catalog_service.dto.MedicineImageResponse;
import com.pharmacy.catalog_service.entity.Medicine;
import com.pharmacy.catalog_service.entity.MedicineImage;
import com.pharmacy.catalog_service.repository.MedicineImageRepo;
import com.pharmacy.catalog_service.repository.MedicineRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicineImageService {

    private final MedicineRepo medicineRepo;
    private final MedicineImageRepo imageRepo;
    private final FileStorageService fileStorageService;

    // ───── Upload nhiều ảnh ─────

    @Transactional
    public List<MedicineImageResponse> uploadImages(Long medicineId, MultipartFile[] files) {
        Medicine medicine = medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));

        if (files == null || files.length == 0) {
            throw new RuntimeException("Phải chọn ít nhất 1 file ảnh");
        }

        boolean hasPrimary = imageRepo.findByMedicineIdAndIsPrimaryTrue(medicineId).isPresent();
        int currentMaxSort = imageRepo.findMaxSortOrder(medicineId);

        List<MedicineImageResponse> results = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            String fileName = fileStorageService.store(files[i]);
            String imageUrl = "/uploads/medicines/" + fileName;

            MedicineImage img = new MedicineImage();
            img.setMedicineId(medicineId);
            img.setImageUrl(imageUrl);
            img.setFileName(fileName);
            img.setSortOrder(currentMaxSort + 1 + i);
            img.setPrimary(!hasPrimary && i == 0); // ảnh đầu tiên auto primary nếu chưa có
            img.setCreatedAt(LocalDateTime.now());

            imageRepo.save(img);
            results.add(MedicineImageResponse.from(img));

            // Sync Medicine.imageUrl nếu đây là ảnh primary
            if (img.isPrimary()) {
                medicine.setImageUrl(imageUrl);
                medicine.setUpdatedAt(LocalDateTime.now());
                medicineRepo.save(medicine);
            }
        }

        return results;
    }

    // ───── Danh sách ảnh ─────

    public List<MedicineImageResponse> listImages(Long medicineId) {
        // validate medicine exists
        medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));

        return imageRepo.findByMedicineIdOrderBySortOrderAsc(medicineId)
                .stream()
                .map(MedicineImageResponse::from)
                .toList();
    }

    // ───── Set Primary ─────

    @Transactional
    public MedicineImageResponse setPrimary(Long medicineId, Long imageId) {
        Medicine medicine = medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));

        MedicineImage target = imageRepo.findByIdAndMedicineId(imageId, medicineId)
                .orElseThrow(() -> new RuntimeException(
                        "Image " + imageId + " không thuộc medicine " + medicineId));

        // Bỏ primary cũ
        imageRepo.findByMedicineIdAndIsPrimaryTrue(medicineId)
                .ifPresent(old -> {
                    old.setPrimary(false);
                    imageRepo.save(old);
                });

        // Set primary mới
        target.setPrimary(true);
        imageRepo.save(target);

        // Sync Medicine.imageUrl
        medicine.setImageUrl(target.getImageUrl());
        medicine.setUpdatedAt(LocalDateTime.now());
        medicineRepo.save(medicine);

        return MedicineImageResponse.from(target);
    }

    // ───── Reorder ─────

    @Transactional
    public List<MedicineImageResponse> reorderImages(Long medicineId, List<Long> imageIds) {
        medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));

        List<MedicineImage> existing = imageRepo.findByMedicineIdOrderBySortOrderAsc(medicineId);

        if (imageIds.size() != existing.size()) {
            throw new RuntimeException(
                    "Danh sách imageIds phải chứa đúng " + existing.size() + " phần tử (nhận được " + imageIds.size() + ")");
        }

        // Validate tất cả ids đều thuộc medicine này
        var existingIds = existing.stream().map(MedicineImage::getId).toList();
        for (Long id : imageIds) {
            if (!existingIds.contains(id)) {
                throw new RuntimeException("Image " + id + " không thuộc medicine " + medicineId);
            }
        }

        // Cập nhật sortOrder
        for (int i = 0; i < imageIds.size(); i++) {
            Long imgId = imageIds.get(i);
            MedicineImage img = existing.stream()
                    .filter(e -> e.getId().equals(imgId))
                    .findFirst()
                    .orElseThrow();
            img.setSortOrder(i);
            imageRepo.save(img);
        }

        return imageRepo.findByMedicineIdOrderBySortOrderAsc(medicineId)
                .stream()
                .map(MedicineImageResponse::from)
                .toList();
    }

    // ───── Delete ─────

    @Transactional
    public void deleteImage(Long medicineId, Long imageId) {
        Medicine medicine = medicineRepo.findById(medicineId)
                .orElseThrow(() -> new RuntimeException("Medicine not found: " + medicineId));

        MedicineImage target = imageRepo.findByIdAndMedicineId(imageId, medicineId)
                .orElseThrow(() -> new RuntimeException(
                        "Image " + imageId + " không thuộc medicine " + medicineId));

        boolean wasPrimary = target.isPrimary();
        String fileName = target.getFileName();

        // Xóa record
        imageRepo.delete(target);

        // Xóa file vật lý
        fileStorageService.delete(fileName);

        // Nếu xóa ảnh primary → auto chọn ảnh mới có sortOrder nhỏ nhất
        if (wasPrimary) {
            List<MedicineImage> remaining = imageRepo.findByMedicineIdOrderBySortOrderAsc(medicineId);
            if (!remaining.isEmpty()) {
                MedicineImage newPrimary = remaining.get(0);
                newPrimary.setPrimary(true);
                imageRepo.save(newPrimary);
                medicine.setImageUrl(newPrimary.getImageUrl());
            } else {
                // Hết ảnh → null
                medicine.setImageUrl(null);
            }
            medicine.setUpdatedAt(LocalDateTime.now());
            medicineRepo.save(medicine);
        }
    }
}
