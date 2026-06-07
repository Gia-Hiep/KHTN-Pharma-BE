package com.pharmacy.catalog_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Path UPLOAD_DIR = Paths.get("uploads", "medicines");

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Validate và lưu file ảnh vào thư mục uploads/medicines/.
     *
     * @param file MultipartFile từ request
     * @return tên file đã lưu (UUID + extension)
     */
    public String store(MultipartFile file) {
        // 1. Validate file không rỗng
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File ảnh không được để trống");
        }

        // 2. Validate dung lượng
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File ảnh vượt quá giới hạn 5MB (kích thước: "
                    + (file.getSize() / 1024) + "KB)");
        }

        // 3. Validate định dạng
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new RuntimeException("Định dạng ảnh không hợp lệ: ." + extension
                    + ". Chỉ chấp nhận: jpg, jpeg, png, webp");
        }

        // 4. Sinh tên file duy nhất
        String fileName = UUID.randomUUID() + "." + extension.toLowerCase();

        try {
            // 5. Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(UPLOAD_DIR);

            // 6. Lưu file
            Path targetPath = UPLOAD_DIR.resolve(fileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage(), e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new RuntimeException("File không có phần mở rộng hợp lệ");
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    /**
     * Xóa file ảnh khỏi thư mục uploads/medicines/.
     *
     * @param fileName tên file cần xóa
     */
    public void delete(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        try {
            Path filePath = UPLOAD_DIR.resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log warning nhưng không throw — xóa file thất bại không nên chặn business logic
            System.err.println("Warning: Không thể xóa file " + fileName + ": " + e.getMessage());
        }
    }
}
