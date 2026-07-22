package br.com.techne.lyceum.academic.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn("[{}] Business error {}: {}", traceId, ex.getCode(), ex.getMessage());

        return buildResponse(
                ex.getStatus(), ex.getCode(), ex.getMessage(), request, traceId, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn("[{}] Access denied on {}: {}", traceId, request.getRequestURI(), ex.getMessage());

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "You do not have permission to perform this action",
                request,
                traceId,
                List.of());
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn(
                "[{}] Authentication failed on {}: {}",
                traceId,
                request.getRequestURI(),
                ex.getMessage());

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "UNAUTHORIZED",
                "Invalid credentials or authentication required",
                request,
                traceId,
                List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        List<ApiErrorResponse.FieldErrorDetail> details =
                ex.getBindingResult().getFieldErrors().stream().map(this::toFieldError).toList();

        log.warn(
                "[{}] Validation failed on {}: {} field error(s)",
                traceId,
                request.getRequestURI(),
                details.size());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "One or more fields are invalid",
                request,
                traceId,
                details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        List<ApiErrorResponse.FieldErrorDetail> details =
                ex.getConstraintViolations().stream()
                        .map(
                                violation ->
                                        new ApiErrorResponse.FieldErrorDetail(
                                                violation.getPropertyPath().toString(),
                                                violation.getMessage(),
                                                violation.getInvalidValue()))
                        .toList();

        log.warn(
                "[{}] Constraint violation on {}: {}",
                traceId,
                request.getRequestURI(),
                ex.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "CONSTRAINT_VIOLATION",
                "Request constraints were violated",
                request,
                traceId,
                details);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        String requiredType =
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = "Parameter '%s' must be of type %s".formatted(ex.getName(), requiredType);

        log.warn("[{}] Type mismatch on {}: {}", traceId, request.getRequestURI(), message);

        return buildResponse(
                HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message, request, traceId, List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        String message = "Required parameter '%s' is missing".formatted(ex.getParameterName());

        log.warn("[{}] Missing parameter on {}: {}", traceId, request.getRequestURI(), message);

        return buildResponse(
                HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message, request, traceId, List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.warn(
                "[{}] Malformed request body on {}: {}",
                traceId,
                request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST",
                "Request body is missing or malformed",
                request,
                traceId,
                List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        String traceId = newTraceId();
        log.error("[{}] Unexpected error on {}", traceId, request.getRequestURI(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred. Use the traceId to correlate with server logs.",
                request,
                traceId,
                List.of());
    }

    private ApiErrorResponse.FieldErrorDetail toFieldError(FieldError fieldError) {
        return new ApiErrorResponse.FieldErrorDetail(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getRejectedValue());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            String traceId,
            List<ApiErrorResponse.FieldErrorDetail> details) {
        ApiErrorResponse body =
                new ApiErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        code,
                        message,
                        request.getRequestURI(),
                        traceId,
                        details);
        return ResponseEntity.status(status).body(body);
    }

    private String newTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
