package com.petify.pet.service;

import com.petify.pet.dto.response.PetCategoryResponse;

import java.util.List;

public interface PetCategoryService {
    
    List<PetCategoryResponse> getAllCategories();
    
    List<PetCategoryResponse> getCategoryTree();
    
    PetCategoryResponse getCategoryById(Long categoryId);
    
    List<PetCategoryResponse> getChildCategories(Long parentId);
}