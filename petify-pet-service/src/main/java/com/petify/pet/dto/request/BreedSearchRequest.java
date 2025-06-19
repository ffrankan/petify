package com.petify.pet.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BreedSearchRequest {
    
    @Size(max = 50, message = "搜索关键词长度不能超过50个字符")
    private String keyword;
    
    private Long categoryId;
    
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;
    
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer size = 20;
    
    private String sizeCategory;
}