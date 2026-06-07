package com.pharmacy.auth_service.exception;

import com.pharmacy.auth_service.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleRuntime(RuntimeException ex) {
        return ApiResponse.fail(ex.getMessage());
    }
}
