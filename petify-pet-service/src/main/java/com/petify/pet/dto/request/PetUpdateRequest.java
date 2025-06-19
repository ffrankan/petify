package com.petify.pet.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PetUpdateRequest {
    
    @NotBlank(message = "宠物名称不能为空")
    @Size(max = 100, message = "宠物名称长度不能超过100个字符")
    private String name;
    
    @NotNull(message = "宠物分类不能为空")
    private Long categoryId;
    
    private Long breedId;
    
    @Pattern(regexp = "^(male|female|unknown)$", message = "性别必须是male、female或unknown")
    private String gender;
    
    @Past(message = "出生日期必须是过去的日期")
    private LocalDate birthDate;
    
    @DecimalMin(value = "0.01", message = "体重必须大于0")
    @DecimalMax(value = "999.99", message = "体重不能超过999.99kg")
    private BigDecimal weight;
    
    @Size(max = 50, message = "颜色描述长度不能超过50个字符")
    private String color;
    
    @Size(max = 50, message = "芯片号长度不能超过50个字符")
    private String microchipNumber;
    
    @Size(max = 500, message = "宠物描述长度不能超过500个字符")
    private String description;
    
    private String avatarUrl;
    
    @Size(max = 1000, message = "医疗备注长度不能超过1000个字符")
    private String medicalNotes;
    
    private Boolean isNeutered;
    
    private Boolean isVaccinated;
    
    @Min(value = 1, message = "状态值无效")
    @Max(value = 3, message = "状态值无效")
    private Integer status;
}