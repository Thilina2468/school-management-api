package com.campus.schoolmanagementapi.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@JsonPropertyOrder({"studentCode", "marks", "createdAt", "updatedAt"})
public class MarkResponseDTO {

    private String studentCode;
    private Map<String, Object> marks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
