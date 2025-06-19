package com.petify.pet.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PetMedicalRecordResponse {
    
    private Long id;
    private Long petId;
    private String recordType;
    private LocalDate recordDate;
    private String veterinarian;
    private String clinicName;
    private String diagnosis;
    private String treatment;
    private String medications;
    private BigDecimal cost;
    private String notes;
    private LocalDateTime createdAt;
}