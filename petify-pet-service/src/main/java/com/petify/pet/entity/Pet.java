package com.petify.pet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "pets")
public class Pet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "owner_id")
    private Long ownerId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "breed_id")
    private Long breedId;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "birth_date")
    private LocalDate birthDate;
    
    @Column(name = "weight")
    private BigDecimal weight;
    
    @Column(name = "color")
    private String color;
    
    @Column(name = "microchip_number")
    private String microchipNumber;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "medical_notes")
    private String medicalNotes;
    
    @Column(name = "is_neutered")
    private Boolean isNeutered;
    
    @Column(name = "is_vaccinated")
    private Boolean isVaccinated;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // 关联关系
    @Transient
    private PetCategory category;
    
    @Transient
    private PetBreed breed;
    
    @Transient
    private List<PetVaccination> vaccinations;
    
    @Transient
    private List<PetMedicalRecord> medicalRecords;
}