package com.taptap.tds.registration.server.util;

import com.taptap.tds.registration.server.ApiResponseDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = WebExchangeBindException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiResponseDto errorHandler(WebExchangeBindException exception) {
        log.error("WebExchangeBindException " + exception.getMessage());
        return new ApiResponseDto(400,"illegal_parameters");
    }

    @ExceptionHandler(value = ServerWebInputException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiResponseDto errorHandler(ServerWebInputException exception) {
        log.error("ServerWebInputException ", exception);
        return new ApiResponseDto(400,"illegal_parameters");
    }

    @ExceptionHandler(value = TypeMismatchException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiResponseDto errorHandler(TypeMismatchException exception) {
        log.error("Type mismatch Exception " + exception.getMessage());
        return new ApiResponseDto(400,"illegal_parameters");
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiResponseDto errorHandler(ConstraintViolationException exception) {
        log.error("Constraint Violation Exception " + exception.getMessage());
        return new ApiResponseDto(400,"illegal_parameters");
    }

    @ExceptionHandler(value = ValidationException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiResponseDto errorHandler(Exception exception) {
        log.error("Validation Exception " + exception.getMessage());
        return new ApiResponseDto(400,"illegal_parameters");
    }


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseBody
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public ApiResponseDto errorHandler(MethodArgumentNotValidException exception) {
        log.error("Method Argument Not Valid Exception " + exception.getMessage());
        return new ApiResponseDto(400,"illegal_parameters");
    }

}
