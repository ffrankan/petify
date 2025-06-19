package com.petify.pet.service.impl;

import com.petify.pet.dto.response.PetCategoryResponse;
import com.petify.pet.entity.PetCategory;
import com.petify.pet.repository.PetCategoryRepository;
import com.petify.pet.service.PetCategoryService;
import com.petify.pet.util.PetConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PetCategoryServiceImpl implements PetCategoryService {
    
    private final PetCategoryRepository petCategoryRepository;
    
    @Override
    @Cacheable(value = "categories", key = "'all'")
    public List<PetCategoryResponse> getAllCategories() {
        log.info("Fetching all pet categories");
        
        List<PetCategory> categories = petCategoryRepository.findAllActive();
        return PetConverter.toCategoryResponseList(categories);
    }
    
    @Override
    @Cacheable(value = "categories", key = "'tree'")
    public List<PetCategoryResponse> getCategoryTree() {
        log.info("Building category tree");
        
        List<PetCategory> allCategories = petCategoryRepository.findAllActive();
        return buildCategoryTree(allCategories);
    }
    
    @Override
    @Cacheable(value = "categories", key = "#categoryId")
    public PetCategoryResponse getCategoryById(Long categoryId) {
        log.info("Fetching category with ID: {}", categoryId);
        
        PetCategory category = petCategoryRepository.findById(categoryId).orElse(null);
        if (category == null || category.getStatus() != 1) {
            return null;
        }
        
        return PetConverter.toCategoryResponse(category);
    }
    
    @Override
    @Cacheable(value = "categories", key = "'children:' + #parentId")
    public List<PetCategoryResponse> getChildCategories(Long parentId) {
        log.info("Fetching child categories for parent ID: {}", parentId);
        
        List<PetCategory> children = petCategoryRepository.findByParentIdAndActive(parentId);
        return PetConverter.toCategoryResponseList(children);
    }
    
    private List<PetCategoryResponse> buildCategoryTree(List<PetCategory> allCategories) {
        Map<Long, List<PetCategory>> childrenMap = allCategories.stream()
                .filter(cat -> cat.getParentId() != null)
                .collect(Collectors.groupingBy(PetCategory::getParentId));
        
        List<PetCategoryResponse> rootCategories = allCategories.stream()
                .filter(cat -> cat.getParentId() == null)
                .map(PetConverter::toCategoryResponse)
                .collect(Collectors.toList());
        
        for (PetCategoryResponse root : rootCategories) {
            setChildren(root, childrenMap);
        }
        
        return rootCategories;
    }
    
    private void setChildren(PetCategoryResponse parent, Map<Long, List<PetCategory>> childrenMap) {
        List<PetCategory> children = childrenMap.get(parent.getId());
        if (children != null && !children.isEmpty()) {
            List<PetCategoryResponse> childResponses = PetConverter.toCategoryResponseList(children);
            parent.setChildren(childResponses);
            
            for (PetCategoryResponse child : childResponses) {
                setChildren(child, childrenMap);
            }
        }
    }
}