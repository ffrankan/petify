package com.petify.pet.service.impl;

import com.petify.pet.dto.request.BreedSearchRequest;
import com.petify.pet.dto.response.PetBreedResponse;
import com.petify.pet.entity.PetBreed;
import com.petify.pet.repository.PetBreedRepository;
import com.petify.pet.service.PetBreedService;
import com.petify.pet.util.PetConverter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetBreedServiceImpl implements PetBreedService {
    
    private final PetBreedRepository petBreedRepository;
    
    @Override
    @Cacheable(value = "breeds", key = "'category:' + #categoryId")
    public List<PetBreedResponse> getBreedsByCategory(Long categoryId) {
        log.info("Fetching breeds for category ID: {}", categoryId);
        
        List<PetBreed> breeds = petBreedRepository.findByCategoryIdAndActive(categoryId);
        return PetConverter.toBreedResponseList(breeds);
    }
    
    @Override
    public Page<PetBreedResponse> searchBreeds(BreedSearchRequest request) {
        log.info("Searching breeds with keyword: {}, categoryId: {}", 
                request.getKeyword(), request.getCategoryId());
        
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize());
        
        // Use repository method to search with criteria
        List<PetBreed> breeds = petBreedRepository.findBySearchCriteria(
                request.getCategoryId(),
                request.getKeyword(),
                request.getSizeCategory()
        );
        
        // Manual pagination for now - could be optimized with Pageable in repository
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), breeds.size());
        List<PetBreed> pageContent = breeds.subList(start, end);
        
        List<PetBreedResponse> responses = PetConverter.toBreedResponseList(pageContent);
        return new org.springframework.data.domain.PageImpl<>(responses, pageable, breeds.size());
    }
    
    @Override
    public List<PetBreedResponse> searchBreedsByKeyword(String keyword, int limit) {
        log.info("Searching breeds by keyword: {} with limit: {}", keyword, limit);
        
        Pageable pageable = PageRequest.of(0, limit);
        List<PetBreed> breeds = petBreedRepository.findByKeywordWithLimit(keyword, pageable);
        return PetConverter.toBreedResponseList(breeds);
    }
    
    @Override
    @Cacheable(value = "breeds", key = "#breedId")
    public PetBreedResponse getBreedById(Long breedId) {
        log.info("Fetching breed with ID: {}", breedId);
        
        PetBreed breed = petBreedRepository.findById(breedId).orElse(null);
        if (breed == null || breed.getStatus() != 1) {
            return null;
        }
        
        return PetConverter.toBreedResponse(breed);
    }
}