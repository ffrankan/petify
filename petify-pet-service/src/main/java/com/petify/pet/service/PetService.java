package com.petify.pet.service;

import org.springframework.data.domain.Page;
import com.petify.pet.dto.request.PetCreateRequest;
import com.petify.pet.dto.request.PetQueryRequest;
import com.petify.pet.dto.request.PetUpdateRequest;
import com.petify.pet.dto.response.PetResponse;

import java.util.List;

public interface PetService {
    
    PetResponse createPet(PetCreateRequest request, Long ownerId);
    
    List<PetResponse> getUserPets(Long userId);
    
    Page<PetResponse> getUserPets(Long userId, PetQueryRequest request);
    
    PetResponse getPetById(Long petId, Long userId);
    
    PetResponse updatePet(Long petId, Long userId, PetUpdateRequest request);
    
    void deletePet(Long petId, Long userId);
    
    void validatePetOwnership(Long petId, Long userId);
}