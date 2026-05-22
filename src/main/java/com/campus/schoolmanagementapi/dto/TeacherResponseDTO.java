package com.campus.schoolmanagementapi.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({"teacherCode", "name", "email", "modules", "createdAt", "updatedAt"})
public class TeacherResponseDTO {

    private String teacherCode;
    private String name;
    private String email;
    private List<String> modules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
