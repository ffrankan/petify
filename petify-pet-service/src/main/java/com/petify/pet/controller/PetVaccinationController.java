package com.petify.pet.controller;

import com.petify.common.result.Result;
import com.petify.pet.context.UserContextHolder;
import com.petify.pet.dto.request.VaccinationCreateRequest;
import com.petify.pet.dto.request.VaccinationUpdateRequest;
import com.petify.pet.dto.response.PetVaccinationResponse;
import com.petify.pet.service.PetVaccinationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pet/pets/{petId}/vaccinations")
@RequiredArgsConstructor
public class PetVaccinationController {
    
    private final PetVaccinationService vaccinationService;
    
    @GetMapping
    public Result<List<PetVaccinationResponse>> getPetVaccinations(@PathVariable Long petId) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        List<PetVaccinationResponse> vaccinations = vaccinationService.getPetVaccinations(petId, userId);
        return Result.success(vaccinations);
    }
    
    @PostMapping
    public Result<PetVaccinationResponse> createVaccination(@PathVariable Long petId,
                                                           @Valid @RequestBody VaccinationCreateRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        PetVaccinationResponse vaccination = vaccinationService.createVaccination(petId, userId, request);
        return Result.success(vaccination);
    }
    
    @PutMapping("/{vaccinationId}")
    public Result<PetVaccinationResponse> updateVaccination(@PathVariable Long petId,
                                                           @PathVariable Long vaccinationId,
                                                           @Valid @RequestBody VaccinationUpdateRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        PetVaccinationResponse vaccination = vaccinationService.updateVaccination(
                petId, vaccinationId, userId, request);
        return Result.success(vaccination);
    }
    
    @DeleteMapping("/{vaccinationId}")
    public Result<Void> deleteVaccination(@PathVariable Long petId,
                                         @PathVariable Long vaccinationId) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        vaccinationService.deleteVaccination(petId, vaccinationId, userId);
        return Result.success();
    }
}

@RestController
@RequestMapping("/api/pet/vaccinations")
@RequiredArgsConstructor
@Slf4j
class VaccinationQueryController {
    
    private final PetVaccinationService vaccinationService;
    
    @GetMapping("/due")
    public Result<List<PetVaccinationResponse>> getDueVaccinations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        if (dueDate == null) {
            dueDate = LocalDate.now().plusDays(30); // 默认30天内到期
        }
        
        List<PetVaccinationResponse> vaccinations = vaccinationService.getDueVaccinations(userId, dueDate);
        return Result.success(vaccinations);
    }
}