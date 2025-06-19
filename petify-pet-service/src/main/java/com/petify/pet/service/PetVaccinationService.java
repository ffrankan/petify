package com.petify.pet.service;

import com.petify.pet.dto.request.VaccinationCreateRequest;
import com.petify.pet.dto.request.VaccinationUpdateRequest;
import com.petify.pet.dto.response.PetVaccinationResponse;

import java.time.LocalDate;
import java.util.List;

public interface PetVaccinationService {
    
    List<PetVaccinationResponse> getPetVaccinations(Long petId, Long userId);
    
    PetVaccinationResponse createVaccination(Long petId, Long userId, VaccinationCreateRequest request);
    
    PetVaccinationResponse updateVaccination(Long petId, Long vaccinationId, Long userId, VaccinationUpdateRequest request);
    
    void deleteVaccination(Long petId, Long vaccinationId, Long userId);
    
    List<PetVaccinationResponse> getDueVaccinations(Long userId, LocalDate dueDate);
}