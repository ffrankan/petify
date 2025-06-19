package com.petify.pet.exception;

import com.petify.common.exception.BusinessException;

public class PetNotFoundException extends BusinessException {
    
    public PetNotFoundException(String message) {
        super(404, message);
    }
    
    public PetNotFoundException(Long petId) {
        super(404, "宠物不存在，ID: " + petId);
    }
}