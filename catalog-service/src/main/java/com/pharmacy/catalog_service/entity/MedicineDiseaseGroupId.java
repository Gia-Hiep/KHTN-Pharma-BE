package com.pharmacy.catalog_service.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key cho MedicineDiseaseGroup
 */
public class MedicineDiseaseGroupId implements Serializable {

    private Long medicineId;
    private Long diseaseGroupId;

    public MedicineDiseaseGroupId() {}

    public MedicineDiseaseGroupId(Long medicineId, Long diseaseGroupId) {
        this.medicineId = medicineId;
        this.diseaseGroupId = diseaseGroupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicineDiseaseGroupId that = (MedicineDiseaseGroupId) o;
        return Objects.equals(medicineId, that.medicineId) &&
               Objects.equals(diseaseGroupId, that.diseaseGroupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(medicineId, diseaseGroupId);
    }
}
