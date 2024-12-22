package ru.beetlink.backend.utils.token

import ru.beetlink.backend.models.entity.user.Role
import org.springframework.security.core.Authentication

/**
 * Represents authentication data extracted from a JWT (JSON Web Token) used for
 * managing user authentication within the security context.
 * This class implements the `Authentication` interface to integrate with Spring Security
 * and provide authentication details based on the user's ID and role.
 *
 * @param authenticated A flag indicating whether the user is authenticated.
 * @param userId The unique identifier of the authenticated user.
 * @param role The role of the user, representing their access permissions.
 */
class JwtAuthData(
    private var authenticated: Boolean,
    private val userId: Long,
    private val role: Role
) : Authentication {
    override fun getAuthorities() = setOf(role)
    override fun getName() = null
    override fun getCredentials() = null
    override fun getDetails() = null
    override fun getPrincipal() = userId
    override fun isAuthenticated() = authenticated
    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }
}