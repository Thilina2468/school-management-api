package com.campus.schoolmanagementapi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModuleAssignDTO {

    @NotNull(message = "Modules list is required")
    private List<String> modules;

}
