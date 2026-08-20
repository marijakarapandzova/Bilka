package com.plantpulse.plantservice.application

class EmailAlreadyRegisteredException(email: String) :
    RuntimeException("Email already registered: $email")

class InvalidCredentialsException :
    RuntimeException("Invalid email or password")

class SpeciesNotFoundException(id: Any) :
    RuntimeException("Species not found: $id")

class PlantNotFoundException(id: Any) :
    RuntimeException("Plant not found: $id")

class PlantAccessDeniedException :
    RuntimeException("This plant does not belong to the current user")

class PhotoIdentificationFailedException(reason: String) :
    RuntimeException("Photo identification failed: $reason")
