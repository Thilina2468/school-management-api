package com.campus.schoolmanagementapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentUpdateDTO {

    @Size(max = 50, message = "Name must be under 50 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

}
