package com.petify.pet.service.impl;

import com.petify.pet.dto.request.VaccinationCreateRequest;
import com.petify.pet.dto.request.VaccinationUpdateRequest;
import com.petify.pet.dto.response.PetVaccinationResponse;
import com.petify.pet.entity.PetVaccination;
import com.petify.pet.exception.VaccinationNotFoundException;
import com.petify.pet.repository.PetVaccinationRepository;
import com.petify.pet.service.PetService;
import com.petify.pet.service.PetVaccinationService;
import com.petify.pet.util.PetConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetVaccinationServiceImpl implements PetVaccinationService {
    
    private final PetVaccinationRepository vaccinationRepository;
    private final PetService petService;
    
    @Override
    public List<PetVaccinationResponse> getPetVaccinations(Long petId, Long userId) {
        log.info("Fetching vaccinations for pet: {} by user: {}", petId, userId);
        
        // 验证宠物所有权
        petService.validatePetOwnership(petId, userId);
        
        List<PetVaccination> vaccinations = vaccinationRepository.findByPetIdAndOwnerId(petId, userId);
        return PetConverter.toVaccinationResponseList(vaccinations);
    }
    
    @Override
    @Transactional
    public PetVaccinationResponse createVaccination(Long petId, Long userId, VaccinationCreateRequest request) {
        log.info("Creating vaccination for pet: {} by user: {}", petId, userId);
        
        // 验证宠物所有权
        petService.validatePetOwnership(petId, userId);
        
        PetVaccination vaccination = new PetVaccination();
        BeanUtils.copyProperties(request, vaccination);
        vaccination.setPetId(petId);
        
        vaccination = vaccinationRepository.save(vaccination);
        
        log.info("Created vaccination with ID: {}", vaccination.getId());
        return PetConverter.toVaccinationResponse(vaccination);
    }
    
    @Override
    @Transactional
    public PetVaccinationResponse updateVaccination(Long petId, Long vaccinationId, Long userId, 
                                                   VaccinationUpdateRequest request) {
        log.info("Updating vaccination: {} for pet: {} by user: {}", vaccinationId, petId, userId);
        
        // 验证宠物所有权和疫苗记录存在性
        PetVaccination vaccination = vaccinationRepository.findByIdAndPetIdAndOwnerId(vaccinationId, petId, userId);
        if (vaccination == null) {
            throw new VaccinationNotFoundException(vaccinationId);
        }
        
        BeanUtils.copyProperties(request, vaccination);
        vaccination = vaccinationRepository.save(vaccination);
        
        log.info("Updated vaccination with ID: {}", vaccinationId);
        return PetConverter.toVaccinationResponse(vaccination);
    }
    
    @Override
    @Transactional
    public void deleteVaccination(Long petId, Long vaccinationId, Long userId) {
        log.info("Deleting vaccination: {} for pet: {} by user: {}", vaccinationId, petId, userId);
        
        // 验证宠物所有权和疫苗记录存在性
        PetVaccination vaccination = vaccinationRepository.findByIdAndPetIdAndOwnerId(vaccinationId, petId, userId);
        if (vaccination == null) {
            throw new VaccinationNotFoundException(vaccinationId);
        }
        
        vaccinationRepository.deleteById(vaccinationId);
        log.info("Deleted vaccination with ID: {}", vaccinationId);
    }
    
    @Override
    public List<PetVaccinationResponse> getDueVaccinations(Long userId, LocalDate dueDate) {
        log.info("Fetching due vaccinations for user: {} before date: {}", userId, dueDate);
        
        List<PetVaccination> vaccinations = vaccinationRepository.findDueVaccinationsByOwnerId(userId, dueDate);
        return PetConverter.toVaccinationResponseList(vaccinations);
    }
}