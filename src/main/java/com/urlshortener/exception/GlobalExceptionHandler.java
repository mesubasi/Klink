package com.urlshortener.exception;

import com.urlshortener.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageService messageService;

    public GlobalExceptionHandler(MessageService messageService) {
        this.messageService = messageService;
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitExceeded(RateLimitExceededException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", messageService.getMessage("error.rate_limit_exceeded_title", "Rate Limit Exceeded"));
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", messageService.getMessage("error.bad_request"));
        body.put("validationErrors", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUrlNotFound(UrlNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", messageService.getMessage("error.not_found"));
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UrlAccessRestrictedException.class)
    public ResponseEntity<Map<String, Object>> handleUrlAccessRestricted(UrlAccessRestrictedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", messageService.getMessage("error.access_restricted", "Access Restricted"));
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MaliciousUrlException.class)
    public ResponseEntity<Map<String, Object>> handleMaliciousUrl(MaliciousUrlException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", messageService.getMessage("error.malicious_url_title", "Malicious URL Detected"));
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", messageService.getMessage("error.bad_request"));
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        log.error("Unhandled internal server error: ", ex);
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", messageService.getMessage("error.general"));
        body.put("message", "Beklenmeyen bir sunucu hatası meydana geldi.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
