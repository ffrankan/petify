package com.petify.pet.util;

import com.petify.pet.dto.request.PetCreateRequest;
import com.petify.pet.dto.request.PetUpdateRequest;
import com.petify.pet.dto.response.*;
import com.petify.pet.entity.*;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

public class PetConverter {
    
    public static Pet toEntity(PetCreateRequest request, Long ownerId) {
        Pet pet = new Pet();
        BeanUtils.copyProperties(request, pet);
        pet.setOwnerId(ownerId);
        pet.setStatus(1); // 默认健康状态
        return pet;
    }
    
    public static void updateEntity(Pet pet, PetUpdateRequest request) {
        BeanUtils.copyProperties(request, pet);
    }
    
    public static PetResponse toResponse(Pet pet) {
        PetResponse response = new PetResponse();
        BeanUtils.copyProperties(pet, response);
        return response;
    }
    
    public static List<PetResponse> toResponseList(List<Pet> pets) {
        return pets.stream()
                .map(PetConverter::toResponse)
                .collect(Collectors.toList());
    }
    
    public static PetCategoryResponse toCategoryResponse(PetCategory category) {
        PetCategoryResponse response = new PetCategoryResponse();
        BeanUtils.copyProperties(category, response);
        return response;
    }
    
    public static List<PetCategoryResponse> toCategoryResponseList(List<PetCategory> categories) {
        return categories.stream()
                .map(PetConverter::toCategoryResponse)
                .collect(Collectors.toList());
    }
    
    public static PetBreedResponse toBreedResponse(PetBreed breed) {
        PetBreedResponse response = new PetBreedResponse();
        BeanUtils.copyProperties(breed, response);
        return response;
    }
    
    public static List<PetBreedResponse> toBreedResponseList(List<PetBreed> breeds) {
        return breeds.stream()
                .map(PetConverter::toBreedResponse)
                .collect(Collectors.toList());
    }
    
    public static PetVaccinationResponse toVaccinationResponse(PetVaccination vaccination) {
        PetVaccinationResponse response = new PetVaccinationResponse();
        BeanUtils.copyProperties(vaccination, response);
        return response;
    }
    
    public static List<PetVaccinationResponse> toVaccinationResponseList(List<PetVaccination> vaccinations) {
        return vaccinations.stream()
                .map(PetConverter::toVaccinationResponse)
                .collect(Collectors.toList());
    }
    
    public static PetMedicalRecordResponse toMedicalRecordResponse(PetMedicalRecord record) {
        PetMedicalRecordResponse response = new PetMedicalRecordResponse();
        BeanUtils.copyProperties(record, response);
        return response;
    }
    
    public static List<PetMedicalRecordResponse> toMedicalRecordResponseList(List<PetMedicalRecord> records) {
        return records.stream()
                .map(PetConverter::toMedicalRecordResponse)
                .collect(Collectors.toList());
    }
}