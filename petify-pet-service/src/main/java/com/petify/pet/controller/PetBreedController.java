package com.petify.pet.controller;

import com.petify.common.result.Result;
import com.petify.pet.dto.request.BreedSearchRequest;
import com.petify.pet.dto.response.PetBreedResponse;
import com.petify.pet.service.PetBreedService;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pet/breeds")
@RequiredArgsConstructor
public class PetBreedController {
    
    private final PetBreedService petBreedService;
    
    @GetMapping
    public Result<Page<PetBreedResponse>> searchBreeds(@Valid BreedSearchRequest request) {
        Page<PetBreedResponse> breeds = petBreedService.searchBreeds(request);
        return Result.success(breeds);
    }
    
    @GetMapping("/search")
    public Result<List<PetBreedResponse>> searchBreedsByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int limit) {
        List<PetBreedResponse> breeds = petBreedService.searchBreedsByKeyword(keyword, limit);
        return Result.success(breeds);
    }
    
    @GetMapping("/{breedId}")
    public Result<PetBreedResponse> getBreedById(@PathVariable Long breedId) {
        PetBreedResponse breed = petBreedService.getBreedById(breedId);
        if (breed == null) {
            return Result.error(404, "品种不存在");
        }
        return Result.success(breed);
    }
    
    @GetMapping("/category/{categoryId}")
    public Result<List<PetBreedResponse>> getBreedsByCategory(@PathVariable Long categoryId) {
        List<PetBreedResponse> breeds = petBreedService.getBreedsByCategory(categoryId);
        return Result.success(breeds);
    }
}