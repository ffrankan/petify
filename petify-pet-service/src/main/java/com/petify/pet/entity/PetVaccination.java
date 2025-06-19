package com.petify.pet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "pet_vaccinations")
public class PetVaccination {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "pet_id")
    private Long petId;
    
    @Column(name = "vaccine_name")
    private String vaccineName;
    
    @Column(name = "vaccination_date")
    private LocalDate vaccinationDate;
    
    @Column(name = "next_due_date")
    private LocalDate nextDueDate;
    
    @Column(name = "veterinarian")
    private String veterinarian;
    
    @Column(name = "clinic_name")
    private String clinicName;
    
    @Column(name = "batch_number")
    private String batchNumber;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    // 关联关系
    @Transient
    private Pet pet;
}