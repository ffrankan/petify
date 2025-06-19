package com.petify.pet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "pet_breeds")
public class PetBreed {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "characteristics")
    private String characteristics;
    
    @Column(name = "average_lifespan")
    private Integer averageLifespan;
    
    @Column(name = "size_category")
    private String sizeCategory;
    
    @Column(name = "origin_country")
    private String originCountry;
    
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
}