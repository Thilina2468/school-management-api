package com.campus.schoolmanagementapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class MarkRequestDTO {

    @NotBlank(message = "Student code is required")
    private String studentCode;

    @NotNull(message = "Marks are required")
    private Map<String, Double> marks;

}
