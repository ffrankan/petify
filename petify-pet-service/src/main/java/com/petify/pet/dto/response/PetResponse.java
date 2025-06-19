package com.petify.pet.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PetResponse {
    
    private Long id;
    private Long ownerId;
    private String name;
    private Long categoryId;
    private Long breedId;
    private String gender;
    private LocalDate birthDate;
    private BigDecimal weight;
    private String color;
    private String microchipNumber;
    private String description;
    private String avatarUrl;
    private String medicalNotes;
    private Boolean isNeutered;
    private Boolean isVaccinated;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private PetCategoryResponse category;
    private PetBreedResponse breed;
    private List<PetVaccinationResponse> vaccinations;
}