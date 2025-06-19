package com.petify.pet.service;

import com.petify.pet.dto.request.BreedSearchRequest;
import com.petify.pet.dto.response.PetBreedResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PetBreedService {
    
    List<PetBreedResponse> getBreedsByCategory(Long categoryId);
    
    Page<PetBreedResponse> searchBreeds(BreedSearchRequest request);
    
    List<PetBreedResponse> searchBreedsByKeyword(String keyword, int limit);
    
    PetBreedResponse getBreedById(Long breedId);
}