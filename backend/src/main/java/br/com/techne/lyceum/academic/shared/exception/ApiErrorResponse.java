package br.com.techne.lyceum.academic.shared.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String traceId,
        List<FieldErrorDetail> details) {

    public record FieldErrorDetail(String field, String message, Object rejectedValue) {}
}
