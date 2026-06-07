package com.pharmacy.catalog_service.repository;

import com.pharmacy.catalog_service.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepo extends JpaRepository<Category, Long> {
}
