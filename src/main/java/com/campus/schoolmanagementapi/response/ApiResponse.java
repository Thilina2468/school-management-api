package com.campus.schoolmanagementapi.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@JsonPropertyOrder({"status", "message", "data"})
public class ApiResponse {

    private int status;
    private String message;
    private Object data;

    public static final int OK = 200;
    public static final int CREATED = 201;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int INTERNAL_ERROR = 500;

    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String TOKEN_EXPIRED = "Token has expired";
    public static final String TOKEN_INVALID = "Invalid token";

    public static final String STUDENT_FETCHED = "Student fetched successfully";
    public static final String STUDENTS_FETCHED = "Students fetched successfully";
    public static final String NO_STUDENTS = "No students found";
    public static final String STUDENT_CREATED = "Student registered successfully";
    public static final String STUDENT_UPDATED = "Student updated successfully";
    public static final String STUDENT_DELETED = "Student deleted successfully";
    public static final String STUDENT_NOT_FOUND = "Student not found";

    public static final String TEACHER_FETCHED = "Teacher fetched successfully";
    public static final String TEACHERS_FETCHED = "Teachers fetched successfully";
    public static final String NO_TEACHERS = "No teachers found";
    public static final String TEACHER_CREATED = "Teacher registered successfully";
    public static final String TEACHER_UPDATED = "Teacher updated successfully";
    public static final String TEACHER_NOT_FOUND = "Teacher not found";

    public static final String MARK_FETCHED = "Mark fetched successfully";
    public static final String MARKS_FETCHED = "Marks fetched successfully";
    public static final String NO_MARKS = "No marks found";
    public static final String MARK_CREATED = "Mark created successfully";
    public static final String MARK_UPDATED = "Mark updated successfully";
    public static final String MARK_DELETED = "Mark deleted successfully";
    public static final String MARK_NOT_FOUND = "Mark not found";

    public static final String DUPLICATE_EMAIL = "Email already exists";
    public static final String MARK_EXISTS = "Mark record already exists for this student";

    public ApiResponse(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static ResponseEntity<ApiResponse> send(int status, String message, Object data) {
        return ResponseEntity.status(status).body(new ApiResponse(status, message, data));
    }

}
