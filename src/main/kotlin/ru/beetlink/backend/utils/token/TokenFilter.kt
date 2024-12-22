package ru.beetlink.backend.utils.token

import ru.beetlink.backend.models.entity.user.Role
import ru.beetlink.backend.models.repository.TokenRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.GenericFilterBean

/**
 * A custom filter for validating and extracting information from access tokens in HTTP requests.
 * This filter checks the "Authorization" header for a Bearer token and validates it using the
 * `TokenManager`. If the token is valid and matches the stored token for the user, it sets
 * the authentication data in the security context.
 *
 * @param tokenManager The `TokenManager` used to validate and extract claims from the JWT token.
 * @param tokenRepo The repository for accessing stored tokens associated with users.
 */
@Component
class TokenFilter(
    @Autowired private val tokenManager: TokenManager,
    @Autowired private val tokenRepo: TokenRepository
) : GenericFilterBean() {

    // The name of the Authorization header.
    private val authHeaderName = "Authorization"

    // The prefix for Bearer tokens in the Authorization header.
    private val authHeaderStart = "Bearer "

    /**
     * Processes the HTTP request and response, validating the access token found in the
     * "Authorization" header. If the token is valid, it extracts the claims and sets the
     * authentication data in the security context.
     *
     * @param request The incoming HTTP request.
     * @param response The outgoing HTTP response.
     * @param chain The filter chain that allows the request to proceed.
     * @throws IOException If an I/O error occurs during filter processing.
     * @throws ServletException If a servlet-specific error occurs during filter processing.
     */
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        (request as HttpServletRequest).getHeader(authHeaderName)?.let { header ->
            // Check if the header has text and starts with the "Bearer " prefix.
            if (StringUtils.hasText(header) && header.startsWith(authHeaderStart)) {
                val token = header.substring(authHeaderStart.length)

                // Validate the token using the TokenManager.
                if (tokenManager.validateAccessToken(token)) {
                    tokenManager.getAccessClaims(token).let { claims ->
                        // Check if the token matches the one stored in the TokenRepository.
                        if (tokenRepo.getTokenByUserId(claims.subject.toLong())?.accessToken == token) {
                            // Create JwtAuthData and set it in the SecurityContextHolder for authentication.
                            val authData = JwtAuthData(
                                userId = claims.subject.toLong(),
                                role = claims.get("roles", String::class.java).let { Role.valueOf(it) },
                                authenticated = true
                            )
                            SecurityContextHolder.getContext().authentication = authData
                        }
                    }
                }
            }
        }
        // Continue the filter chain.
        chain.doFilter(request, response)
    }
}