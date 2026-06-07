package com.pharmacy.catalog_service.controller;

import com.pharmacy.catalog_service.dto.CreateCategoryRequest;
import com.pharmacy.catalog_service.entity.Category;
import com.pharmacy.catalog_service.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CatalogService service;

    @GetMapping
    public List<Category> list() {
        return service.listCategories();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public Category create(@RequestBody CreateCategoryRequest req) {
        return service.createCategory(req);
    }
}
