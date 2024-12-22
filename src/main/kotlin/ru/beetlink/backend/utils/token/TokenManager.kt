package ru.beetlink.backend.utils.token

import ru.beetlink.backend.models.entity.user.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * A component that manages the generation, validation, and extraction of claims from JWT (JSON Web Tokens).
 * It supports both access and refresh tokens, using different secret keys for each.
 *
 * @param jwtSecretAccess The secret key used for signing the access tokens.
 * @param jwtSecretRefresh The secret key used for signing the refresh tokens.
 */
@Component
class TokenManager(
    @Value("\${jwt.secret.access}") jwtSecretAccess: String,
    @Value("\${jwt.secret.refresh}") jwtSecretRefresh: String
) {
    private val jwtSecretAccess = Keys.hmacShaKeyFor(jwtSecretAccess.toByteArray())
    private val jwtSecretRefresh = Keys.hmacShaKeyFor(jwtSecretRefresh.toByteArray())

    // The expiration time for access tokens (1 day).
    private val accessTokenLimit = 60 * 60 * 24

    // The expiration time for refresh tokens (30 days).
    private val refreshTokenLimit = 60 * 60 * 24 * 30

    /**
     * Generates an access token for the given user.
     *
     * @param user The user for whom the access token is being generated.
     * @return A signed JWT representing the access token for the user.
     */
    fun generateAccessToken(user: User): String {
        return Date().let { date ->
            Jwts.builder()
                .setSubject(user.id.toString())
                .setIssuedAt(date)
                .setExpiration(Date(date.time + accessTokenLimit * 1000L))
                .claim("roles", user.role)
                .signWith(jwtSecretAccess)
                .compact()
        }
    }

    /**
     * Generates a refresh token for the given user.
     *
     * @param user The user for whom the refresh token is being generated.
     * @return A signed JWT representing the refresh token for the user.
     */
    fun generateRefreshToken(user: User): String {
        return Date().let { date ->
            Jwts.builder()
                .setSubject(user.id.toString())
                .setIssuedAt(date)
                .setExpiration(Date(date.time + refreshTokenLimit * 1000L))
                .signWith(jwtSecretRefresh)
                .compact()
        }
    }

    /**
     * Validates the provided access token.
     *
     * @param token The access token to validate.
     * @return A boolean indicating whether the token is valid or not.
     */
    fun validateAccessToken(token: String) = validateToken(token, jwtSecretAccess)

    /**
     * Validates the provided refresh token.
     *
     * @param token The refresh token to validate.
     * @return A boolean indicating whether the token is valid or not.
     */
    fun validateRefreshToken(token: String) = validateToken(token, jwtSecretRefresh)

    /**
     * Validates a JWT token using the provided secret key.
     *
     * @param token The JWT token to validate.
     * @param key The secret key used to validate the token.
     * @return A boolean indicating whether the token is valid or not.
     */
    private fun validateToken(token: String, key: Key): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: Exception) {
            println(e.message)
            false
        }
    }

    /**
     * Extracts the claims from the access token.
     *
     * @param token The access token from which the claims are to be extracted.
     * @return The claims associated with the access token.
     */
    fun getAccessClaims(token: String) = getClaims(token, jwtSecretAccess)

    /**
     * Extracts the claims from the refresh token.
     *
     * @param token The refresh token from which the claims are to be extracted.
     * @return The claims associated with the refresh token.
     */
    fun getRefreshClaims(token: String) = getClaims(token, jwtSecretRefresh)

    /**
     * Extracts the claims from a JWT token using the provided secret key.
     *
     * @param token The JWT token from which the claims are to be extracted.
     * @param secret The secret key used to validate the token.
     * @return The claims extracted from the token.
     */
    private fun getClaims(token: String, secret: Key): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secret)
            .build()
            .parseClaimsJws(token)
            .body
    }
}