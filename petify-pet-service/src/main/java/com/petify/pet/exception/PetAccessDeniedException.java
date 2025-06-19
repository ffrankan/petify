package com.petify.pet.exception;

import com.petify.common.exception.BusinessException;

public class PetAccessDeniedException extends BusinessException {
    
    public PetAccessDeniedException(String message) {
        super(403, message);
    }
    
    public PetAccessDeniedException() {
        super(403, "无权访问该宠物信息");
    }
}