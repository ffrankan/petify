package com.petify.pet.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.petify.pet.dto.request.PetCreateRequest;
import com.petify.pet.dto.request.PetQueryRequest;
import com.petify.pet.dto.request.PetUpdateRequest;
import com.petify.pet.dto.response.PetResponse;
import com.petify.pet.entity.Pet;
import com.petify.pet.exception.PetAccessDeniedException;
import com.petify.pet.exception.PetNotFoundException;
import com.petify.pet.repository.PetRepository;
import com.petify.pet.service.PetService;
import com.petify.pet.util.PetConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {
    
    private final PetRepository petRepository;
    
    @Override
    @Transactional
    public PetResponse createPet(PetCreateRequest request, Long ownerId) {
        log.info("Creating pet with name: {} for owner: {}", request.getName(), ownerId);
        
        Pet pet = PetConverter.toEntity(request, ownerId);
        pet = petRepository.save(pet);
        
        log.info("Created pet with ID: {}", pet.getId());
        return PetConverter.toResponse(pet);
    }
    
    @Override
    public List<PetResponse> getUserPets(Long userId) {
        log.info("Fetching pets for user: {}", userId);
        
        List<Pet> pets = petRepository.findByOwnerId(userId);
        return PetConverter.toResponseList(pets);
    }
    
    @Override
    public Page<PetResponse> getUserPets(Long userId, PetQueryRequest request) {
        log.info("Fetching pets for user: {} with pagination", userId);
        
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<Pet> petPage = petRepository.findAll(pageable);
        
        return petPage.map(PetConverter::toResponse);
    }
    
    @Override
    public PetResponse getPetById(Long petId, Long userId) {
        log.info("Fetching pet with ID: {} for user: {}", petId, userId);
        
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        
        return PetConverter.toResponse(pet);
    }
    
    @Override
    @Transactional
    public PetResponse updatePet(Long petId, Long userId, PetUpdateRequest request) {
        log.info("Updating pet with ID: {} for user: {}", petId, userId);
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        
        if (!pet.getOwnerId().equals(userId)) {
            throw new PetAccessDeniedException();
        }
        
        PetConverter.updateEntity(pet, request);
        pet = petRepository.save(pet);
        
        log.info("Updated pet with ID: {}", petId);
        return PetConverter.toResponse(pet);
    }
    
    @Override
    @Transactional
    public void deletePet(Long petId, Long userId) {
        log.info("Deleting pet with ID: {} for user: {}", petId, userId);
        
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        
        if (!pet.getOwnerId().equals(userId)) {
            throw new PetAccessDeniedException();
        }
        
        // 软删除 - 设置状态为已故
        pet.setStatus(3);
        petRepository.save(pet);
        
        log.info("Deleted pet with ID: {}", petId);
    }
    
    @Override
    public void validatePetOwnership(Long petId, Long userId) {
        Pet pet = petRepository.findByIdAndOwnerId(petId, userId)
                .orElseThrow(() -> new PetAccessDeniedException());
                
        if (pet.getStatus() == 3) {
            throw new PetAccessDeniedException();
        }
    }
}