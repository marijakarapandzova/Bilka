package com.plantpulse.plantservice.api

import com.plantpulse.plantservice.application.EmailAlreadyRegisteredException
import com.plantpulse.plantservice.application.InvalidCredentialsException
import com.plantpulse.plantservice.application.PhotoIdentificationFailedException
import com.plantpulse.plantservice.application.PlantAccessDeniedException
import com.plantpulse.plantservice.application.PlantNotFoundException
import com.plantpulse.plantservice.application.SpeciesNotFoundException
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

    @ExceptionHandler(EmailAlreadyRegisteredException::class)
    fun handleEmailTaken(ex: EmailAlreadyRegisteredException): ResponseEntity<ApiError> =
        respond(HttpStatus.CONFLICT, ex.message)

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ApiError> =
        respond(HttpStatus.UNAUTHORIZED, ex.message)

    @ExceptionHandler(SpeciesNotFoundException::class, PlantNotFoundException::class)
    fun handleNotFound(ex: RuntimeException): ResponseEntity<ApiError> =
        respond(HttpStatus.NOT_FOUND, ex.message)

    @ExceptionHandler(PlantAccessDeniedException::class)
    fun handleAccessDenied(ex: PlantAccessDeniedException): ResponseEntity<ApiError> =
        respond(HttpStatus.FORBIDDEN, ex.message)

    @ExceptionHandler(PhotoIdentificationFailedException::class)
    fun handlePhotoIdFailed(ex: PhotoIdentificationFailedException): ResponseEntity<ApiError> =
        respond(HttpStatus.BAD_GATEWAY, ex.message)

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
