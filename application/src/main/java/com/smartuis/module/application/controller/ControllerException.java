package com.smartuis.module.application.controller;

import com.smartuis.module.persistence.Exceptions.UnitsTimeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class ControllerException {

    @ExceptionHandler(UnitsTimeException.class)
    public ResponseEntity handlerUnitsTimeException(UnitsTimeException unitsTimeException){
        return ResponseEntity.badRequest().body(unitsTimeException.getMessage());
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity DateTimeParseException(DateTimeParseException dateTimeParseException){
        return ResponseEntity.badRequest().body("El formato de fecha debe ser (AAAA-MM-DD)");
    }
}
