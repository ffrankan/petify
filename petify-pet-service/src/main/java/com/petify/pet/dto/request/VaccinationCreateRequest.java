package com.petify.pet.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VaccinationCreateRequest {
    
    @NotBlank(message = "疫苗名称不能为空")
    @Size(max = 100, message = "疫苗名称长度不能超过100个字符")
    private String vaccineName;
    
    @NotNull(message = "接种日期不能为空")
    @PastOrPresent(message = "接种日期不能是未来日期")
    private LocalDate vaccinationDate;
    
    @Future(message = "下次接种日期必须是未来日期")
    private LocalDate nextDueDate;
    
    @Size(max = 100, message = "兽医师姓名长度不能超过100个字符")
    private String veterinarian;
    
    @Size(max = 100, message = "诊所名称长度不能超过100个字符")
    private String clinicName;
    
    @Size(max = 50, message = "批次号长度不能超过50个字符")
    private String batchNumber;
    
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String notes;
}