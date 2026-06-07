package com.pharmacy.catalog_service.dto;

import java.util.List;

public record ReorderImagesRequest(
        List<Long> imageIds
) {}
