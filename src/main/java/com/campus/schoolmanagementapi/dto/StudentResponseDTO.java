package com.campus.schoolmanagementapi.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({"studentCode", "name", "email", "status", "modules", "createdAt", "updatedAt"})
public class StudentResponseDTO {

    private String studentCode;
    private String name;
    private String email;
    private String status;
    private List<String> modules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
