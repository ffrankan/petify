package com.petify.pet.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PetBreedResponse {
    
    private Long id;
    private Long categoryId;
    private String name;
    private String description;
    private String characteristics;
    private Integer averageLifespan;
    private String sizeCategory;
    private String originCountry;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PetCategoryResponse category;
}