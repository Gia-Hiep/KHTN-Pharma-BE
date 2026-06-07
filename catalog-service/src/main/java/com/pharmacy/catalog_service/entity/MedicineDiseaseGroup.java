package com.pharmacy.catalog_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Mapping nhiều-nhiều giữa Medicine và DiseaseGroup
 */
@Entity
@Table(name = "medicine_disease_groups")
@Getter
@Setter
@IdClass(MedicineDiseaseGroupId.class)
public class MedicineDiseaseGroup {

    @Id
    @Column(name = "medicine_id", nullable = false)
    private Long medicineId;

    @Id
    @Column(name = "disease_group_id", nullable = false)
    private Long diseaseGroupId;
}
