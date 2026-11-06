package com.ludus.api.controllers;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
@RestControllerAdvice
public class ApiErrors {
    public record Error(int status,String error) {}
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<Error> status(ResponseStatusException e) {
        var builder=ResponseEntity.status(e.getStatusCode());
        if(e.getStatusCode().value()==503) builder.header("Retry-After","1");
        return builder.body(new Error(e.getStatusCode().value(),e.getReason()==null?"Solicitud rechazada":e.getReason()));
    }
    @ExceptionHandler({IllegalArgumentException.class,MethodArgumentTypeMismatchException.class,HttpMessageNotReadableException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class})
    ResponseEntity<Error> bad(Exception e) {return ResponseEntity.badRequest().body(new Error(400,"Entrada invalida"));}
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Error> conflict(Exception e) {return ResponseEntity.status(409).body(new Error(409,"Operacion incompatible con datos relacionados"));}
}
