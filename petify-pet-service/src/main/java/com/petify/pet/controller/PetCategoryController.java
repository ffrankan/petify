package com.petify.pet.controller;

import com.petify.common.result.Result;
import com.petify.pet.dto.response.PetCategoryResponse;
import com.petify.pet.service.PetCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pet/categories")
@RequiredArgsConstructor
public class PetCategoryController {
    
    private final PetCategoryService petCategoryService;
    
    @GetMapping
    public Result<List<PetCategoryResponse>> getAllCategories() {
        List<PetCategoryResponse> categories = petCategoryService.getAllCategories();
        return Result.success(categories);
    }
    
    @GetMapping("/tree")
    public Result<List<PetCategoryResponse>> getCategoryTree() {
        List<PetCategoryResponse> categoryTree = petCategoryService.getCategoryTree();
        return Result.success(categoryTree);
    }
    
    @GetMapping("/{categoryId}")
    public Result<PetCategoryResponse> getCategoryById(@PathVariable Long categoryId) {
        PetCategoryResponse category = petCategoryService.getCategoryById(categoryId);
        if (category == null) {
            return Result.error(404, "分类不存在");
        }
        return Result.success(category);
    }
    
    @GetMapping("/{categoryId}/children")
    public Result<List<PetCategoryResponse>> getChildCategories(@PathVariable Long categoryId) {
        List<PetCategoryResponse> children = petCategoryService.getChildCategories(categoryId);
        return Result.success(children);
    }
}