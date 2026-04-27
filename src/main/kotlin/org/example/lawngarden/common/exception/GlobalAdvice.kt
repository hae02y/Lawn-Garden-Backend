package org.example.lawngarden.common.exception

import io.jsonwebtoken.JwtException
import org.example.lawngarden.common.exception.custom.EmailExistException
import org.example.lawngarden.common.exception.custom.UserExistException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.util.NoSuchElementException

@RestControllerAdvice
class GlobalAdvice {
    @ExceptionHandler(EmailExistException::class, UserExistException::class)
    fun handleConflict(e: RuntimeException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(errorBody("CONFLICT", e.message))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(errorBody("BAD_REQUEST", e.message))
    }

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(errorBody("NOT_FOUND", e.message))
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleForbidden(e: AccessDeniedException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(errorBody("FORBIDDEN", e.message))
    }

    @ExceptionHandler(JwtException::class)
    fun handleUnauthorized(e: JwtException): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(errorBody("UNAUTHORIZED", e.message))
    }

    @ExceptionHandler(Exception::class)
    fun handleServerError(e: Exception): ResponseEntity<ApiErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorBody("INTERNAL_SERVER_ERROR", e.message))
    }

    private fun errorBody(code: String, message: String?): ApiErrorResponse =
        ApiErrorResponse(
            errorCode = code,
            message = message ?: "Unexpected server error",
        )
}
