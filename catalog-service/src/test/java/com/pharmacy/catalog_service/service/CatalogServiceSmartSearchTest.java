package com.pharmacy.catalog_service.service;

import com.pharmacy.catalog_service.dto.SmartMedicineSearchResponse;
import com.pharmacy.catalog_service.entity.Category;
import com.pharmacy.catalog_service.entity.DiseaseGroup;
import com.pharmacy.catalog_service.entity.Medicine;
import com.pharmacy.catalog_service.entity.MedicineDiseaseGroup;
import com.pharmacy.catalog_service.repository.CategoryRepo;
import com.pharmacy.catalog_service.repository.DiseaseGroupRepo;
import com.pharmacy.catalog_service.repository.MedicineDiseaseGroupRepo;
import com.pharmacy.catalog_service.repository.MedicinePriceHistoryRepo;
import com.pharmacy.catalog_service.repository.MedicinePricingTierRepo;
import com.pharmacy.catalog_service.repository.MedicineRepo;
import com.pharmacy.catalog_service.repository.MedicineUnitRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceSmartSearchTest {

    @Mock
    private CategoryRepo categoryRepo;
    @Mock
    private MedicineRepo medicineRepo;
    @Mock
    private MedicinePriceHistoryRepo priceHistoryRepo;
    @Mock
    private MedicinePricingTierRepo pricingTierRepo;
    @Mock
    private MedicineUnitRepo medicineUnitRepo;
    @Mock
    private DiseaseGroupRepo diseaseGroupRepo;
    @Mock
    private MedicineDiseaseGroupRepo medicineDiseaseGroupRepo;
    @Mock
    private FileStorageService fileStorageService;

    private CatalogService service;

    @BeforeEach
    void setUp() {
        service = new CatalogService(
                categoryRepo,
                medicineRepo,
                priceHistoryRepo,
                pricingTierRepo,
                medicineUnitRepo,
                diseaseGroupRepo,
                medicineDiseaseGroupRepo,
                fileStorageService
        );
    }

    @Test
    void smartSearchFindsProductByDiseaseGroupKeywordAfterPrefixCleanup() {
        Medicine medicine = new Medicine();
        medicine.setId(2L);
        medicine.setCode("PARA_500");
        medicine.setName("Paracetamol 500mg");
        medicine.setGenericName("Paracetamol");
        medicine.setSalePrice(new BigDecimal("2000"));
        medicine.setCategoryId(1L);
        medicine.setStatus("ACTIVE");

        Category category = new Category();
        category.setId(1L);
        category.setName("OTC");

        DiseaseGroup group = new DiseaseGroup();
        group.setId(10L);
        group.setCode("ho_hap");
        group.setName("Ho hap");
        group.setKeywords("ho,cam cum,so mui");

        MedicineDiseaseGroup mapping = new MedicineDiseaseGroup();
        mapping.setMedicineId(2L);
        mapping.setDiseaseGroupId(10L);

        when(medicineRepo.findAll()).thenReturn(List.of(medicine));
        when(categoryRepo.findAll()).thenReturn(List.of(category));
        when(diseaseGroupRepo.findAll()).thenReturn(List.of(group));
        when(medicineDiseaseGroupRepo.findAll()).thenReturn(List.of(mapping));

        List<SmartMedicineSearchResponse> results = service.smartSearchMedicines("benh cam cum", 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).id()).isEqualTo(2L);
        assertThat(results.get(0).matchType()).isEqualTo("DISEASE_GROUP");
        assertThat(results.get(0).matchLabel()).contains("Ho hap");
        assertThat(results.get(0).matchedKeyword()).isEqualTo("cam cum");
    }
}
