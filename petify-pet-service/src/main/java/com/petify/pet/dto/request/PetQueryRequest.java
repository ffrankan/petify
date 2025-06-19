package com.petify.pet.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PetQueryRequest {
    
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;
    
    @Min(value = 1, message = "每页数量必须大于0")  
    private Integer size = 20;
    
    private Long categoryId;
    
    private Long breedId;
    
    private String gender;
    
    private Integer status;
}