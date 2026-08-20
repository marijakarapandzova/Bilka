package com.plantpulse.healthservice.api.shared

import com.plantpulse.healthservice.application.NotificationNotFoundException
import com.plantpulse.healthservice.application.PlantHealthNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

data class ApiError(
    val timestamp: Instant = Instant.now(),
    val status: Int,
    val error: String,
    val message: String?
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(PlantHealthNotFoundException::class, NotificationNotFoundException::class)
    fun handleNotFound(ex: RuntimeException): ResponseEntity<ApiError> =
        respond(HttpStatus.NOT_FOUND, ex.message)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val message = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return respond(HttpStatus.BAD_REQUEST, message)
    }

    private fun respond(status: HttpStatus, message: String?): ResponseEntity<ApiError> =
        ResponseEntity.status(status).body(
            ApiError(status = status.value(), error = status.reasonPhrase, message = message)
        )
}
