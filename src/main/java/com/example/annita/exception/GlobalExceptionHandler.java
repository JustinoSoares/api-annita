package com.example.annita.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Acesso negado. Você não tem permissão para acessar este recurso."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", details));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, String>> handleMethodValidation(HandlerMethodValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Dados inválidos. Verifique os campos enviados."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException ex) {
        String msg = "Corpo da requisição inválido. Verifique o formato dos dados enviados.";
        if (ex.getCause() instanceof InvalidFormatException ife) {
            String fieldName = ife.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .filter(f -> f != null)
                    .findFirst()
                    .orElse(null);
            if (fieldName != null) {
                msg = "O campo '" + fieldName + "' possui um valor inválido.";
            }
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", msg));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, String>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "O parâmetro '" + ex.getParameterName() + "' é obrigatório."));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrity(org.springframework.dao.DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Operação não permitida. Este registro possui dependências no sistema."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erro interno do servidor. Tente novamente mais tarde."));
    }
}
