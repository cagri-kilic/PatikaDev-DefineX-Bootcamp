package com.patikadev.definex.advancedtaskmanager.model.dto.request.department;

import com.patikadev.definex.advancedtaskmanager.constant.RegexPatterns;
import com.patikadev.definex.advancedtaskmanager.constant.ValidationMessages;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDepartmentRequest {
    @Size(min = 2, max = 100, message = ValidationMessages.DEPARTMENT_NAME_SIZE)
    @Pattern(regexp = RegexPatterns.DEPARTMENT_NAME_PATTERN, message = ValidationMessages.DEPARTMENT_NAME_PATTERN)
    private String name;

    @Size(max = 500, message = ValidationMessages.DEPARTMENT_DESCRIPTION_MAX_SIZE)
    private String description;
} 