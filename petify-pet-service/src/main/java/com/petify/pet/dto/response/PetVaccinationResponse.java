package com.petify.pet.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PetVaccinationResponse {
    
    private Long id;
    private Long petId;
    private String vaccineName;
    private LocalDate vaccinationDate;
    private LocalDate nextDueDate;
    private String veterinarian;
    private String clinicName;
    private String batchNumber;
    private String notes;
    private LocalDateTime createdAt;
}