package com.petify.pet.controller;

import com.petify.common.result.Result;
import org.springframework.data.domain.Page;
import com.petify.pet.context.UserContextHolder;
import com.petify.pet.dto.request.PetCreateRequest;
import com.petify.pet.dto.request.PetQueryRequest;
import com.petify.pet.dto.request.PetUpdateRequest;
import com.petify.pet.dto.response.PetResponse;
import com.petify.pet.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pet/pets")
@RequiredArgsConstructor
public class PetController {
    
    private final PetService petService;
    
    @PostMapping
    public Result<PetResponse> createPet(@Valid @RequestBody PetCreateRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        PetResponse response = petService.createPet(request, userId);
        return Result.success(response);
    }
    
    @GetMapping
    public Result<List<PetResponse>> getUserPets() {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        List<PetResponse> pets = petService.getUserPets(userId);
        return Result.success(pets);
    }
    
    @GetMapping("/page")
    public Result<Page<PetResponse>> getUserPetsWithPagination(@Valid PetQueryRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        Page<PetResponse> pets = petService.getUserPets(userId, request);
        return Result.success(pets);
    }
    
    @GetMapping("/{petId}")
    public Result<PetResponse> getPetById(@PathVariable Long petId) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        PetResponse pet = petService.getPetById(petId, userId);
        return Result.success(pet);
    }
    
    @PutMapping("/{petId}")
    public Result<PetResponse> updatePet(@PathVariable Long petId, 
                                        @Valid @RequestBody PetUpdateRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        PetResponse pet = petService.updatePet(petId, userId, request);
        return Result.success(pet);
    }
    
    @DeleteMapping("/{petId}")
    public Result<Void> deletePet(@PathVariable Long petId) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        petService.deletePet(petId, userId);
        return Result.success();
    }
}