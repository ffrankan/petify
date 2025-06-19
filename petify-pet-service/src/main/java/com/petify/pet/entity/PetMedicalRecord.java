package com.petify.pet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "pet_medical_records")
public class PetMedicalRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pet_id")
    private Long petId;
    
    @Column(name = "record_type")
    private String recordType;
    
    @Column(name = "record_date")
    private LocalDate recordDate;
    
    @Column(name = "veterinarian")
    private String veterinarian;
    
    @Column(name = "clinic_name")
    private String clinicName;
    
    @Column(name = "diagnosis")
    private String diagnosis;
    
    @Column(name = "treatment")
    private String treatment;
    
    @Column(name = "medications")
    private String medications;
    
    @Column(name = "cost")
    private BigDecimal cost;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 关联关系
    @Transient
    private Pet pet;
}