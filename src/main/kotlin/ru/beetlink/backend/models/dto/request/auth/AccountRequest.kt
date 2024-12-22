package ru.beetlink.backend.models.dto.request.auth

import ru.beetlink.backend.models.entity.user.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

/**
 * Represents the input data for creating or updating an account.
 *
 * @property email The email address of the account. Must not be blank and should follow a valid email format.
 * @property password The password for the account. Must not be blank and should have a minimum length of 8 characters.
 */
data class AccountInput(
    @field:NotBlank
    @field:Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
    val email: String,

    @field:NotBlank
    @field:Size(min = 8)
    val password: String
)

/**
 * Converts the AccountInput object to a User entity.
 *
 * @return A User entity populated with the email and password from the AccountInput.
 */
fun AccountInput.toEntity() = User(
    email = email,
    password = password
)