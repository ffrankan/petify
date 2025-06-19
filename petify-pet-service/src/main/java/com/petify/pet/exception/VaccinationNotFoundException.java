package com.petify.pet.exception;

import com.petify.common.exception.BusinessException;

public class VaccinationNotFoundException extends BusinessException {
    
    public VaccinationNotFoundException(String message) {
        super(404, message);
    }
    
    public VaccinationNotFoundException(Long vaccinationId) {
        super(404, "疫苗记录不存在，ID: " + vaccinationId);
    }
}