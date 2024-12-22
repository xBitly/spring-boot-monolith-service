package ru.beetlink.backend.services

import jakarta.servlet.http.HttpServletRequest
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import ru.beetlink.backend.models.dto.request.link.LinkRequest
import ru.beetlink.backend.models.dto.request.link.toEntity
import ru.beetlink.backend.models.dto.response.link.LinkInfo
import ru.beetlink.backend.models.dto.response.link.LinkStatisticInfo
import ru.beetlink.backend.models.dto.response.link.toDto
import ru.beetlink.backend.models.entity.link.LinkStatistic
import ru.beetlink.backend.models.repository.LinkRepository
import ru.beetlink.backend.models.repository.UserRepository
import ru.beetlink.backend.utils.exception.NotFoundException
import ru.beetlink.backend.utils.exception.AccessDeniedException
import java.time.LocalDate

/**
 * Service class for managing `Link` entities and related operations.
 * This class contains business logic for creating, retrieving, updating, and deleting links,
 * as well as gathering link statistics.
 */
@Service
class LinkService(
    @Autowired private val linkRepository: LinkRepository,
    @Autowired private val userRepository: UserRepository
) {

    /**
     * Creates a new link associated with a specific user.
     *
     * @param userId The ID of the user who owns the link.
     * @param linkRequest The request containing link data.
     * @return The newly created `Link` as a DTO.
     * @throws NotFoundException If the user with the given ID does not exist.
     */
    fun createLink(userId: Long, linkRequest: LinkRequest): LinkInfo {
        userRepository.getUserById(userId)?.let { user ->
            val link = linkRepository.save(linkRequest.toEntity(user))
            link.generateShortId()
            val savedLink = linkRepository.save(link)
            return savedLink.toDto()
        } ?: throw NotFoundException("пользователь не найден")
    }

    /**
     * Retrieves a link's redirect URL based on its short ID and user device type.
     * Statistics are collected for the link upon access.
     *
     * @param shortId The short ID of the link.
     * @param request The HTTP request containing user information.
     * @return The appropriate URL for the user's device type.
     * @throws NotFoundException If the link with the given short ID does not exist.
     */
    fun getLinkByShortId(shortId: String, request: HttpServletRequest): String {
        linkRepository.getLinkByShortId(shortId)?.let { link ->
            val statistic = LinkStatistic(
                link = link,
                ipAddress = getClientIp(request),
                language = detectUserLanguage(request),
                deviceType = detectDeviceType(request) ?: "неизвестно",
                referer = detectReferer(request) ?: "прямое посещение"
            )
            link.addStatistic(statistic)
            return when (statistic.deviceType) {
                "ios" -> link.iosUrl ?: link.defaultUrl
                "android" -> link.androidUrl ?: link.defaultUrl
                "desktop" -> link.desktopUrl ?: link.defaultUrl
                else -> link.defaultUrl
            }
        } ?: throw NotFoundException("ссылка не найдена")
    }

    /**
     * Retrieves a link by its ID, ensuring the user has access to it.
     *
     * @param userId The ID of the user requesting the link.
     * @param linkId The ID of the link.
     * @return The `Link` as a DTO.
     * @throws AccessDeniedException If the user does not own the link.
     * @throws NotFoundException If the link does not exist.
     */
    fun getLinkById(userId: Long, linkId: Long): LinkInfo {
        linkRepository.getLinkById(linkId)?.let { link ->
            if (link.user.id == userId) {
                return link.toDto()
            } else {
                throw AccessDeniedException()
            }
        } ?: throw NotFoundException("ссылка не найдена")
    }

    /**
     * Updates an existing link's data.
     *
     * @param userId The ID of the user who owns the link.
     * @param linkId The ID of the link to update.
     * @param linkRequest The request containing updated link data.
     * @throws AccessDeniedException If the user does not own the link.
     * @throws NotFoundException If the link does not exist.
     */
    fun updateLink(userId: Long, linkId: Long, linkRequest: LinkRequest) {
        linkRepository.getLinkById(linkId)?.let { link ->
            if (link.user.id == userId) {
                link.iosUrl = linkRequest.iosUrl
                link.androidUrl = linkRequest.androidUrl
                link.desktopUrl = linkRequest.desktopUrl
                link.defaultUrl = linkRequest.defaultUrl
                link.description = linkRequest.description
                linkRepository.save(link)
            } else {
                throw AccessDeniedException()
            }
        } ?: throw NotFoundException("ссылка не найдена")
    }

    /**
     * Deletes a link by its ID.
     *
     * @param linkId The ID of the link to delete.
     */
    @Transactional
    fun deleteLink(linkId: Long) = linkRepository.removeLinkById(linkId)

    /**
     * Retrieves statistics for a link within a specific date range.
     *
     * @param userId The ID of the user requesting the statistics.
     * @param linkId The ID of the link.
     * @param startDate The start date for statistics retrieval.
     * @param endDate The end date for statistics retrieval.
     * @return A list of `LinkStatistic` DTOs for the specified date range.
     * @throws AccessDeniedException If the user does not own the link.
     * @throws NotFoundException If the link does not exist.
     */
    fun getLinkStatistics(userId: Long, linkId: Long, startDate: LocalDate, endDate: LocalDate): List<LinkStatisticInfo> {
        linkRepository.getLinkById(linkId)?.let { link ->
            if (link.user.id == userId) {
                return link.getDailyStatistics(startDate, endDate).map { it.toDto() }
            } else {
                throw AccessDeniedException()
            }
        } ?: throw NotFoundException("ссылка не найдена")
    }

    /**
     * Extracts the client's IP address from the request, considering any proxy headers.
     *
     * @param request The HTTP request containing client information.
     * @return The IP address of the client.
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (xForwardedFor.isNullOrEmpty()) {
            request.remoteAddr
        } else {
            xForwardedFor.split(",").firstOrNull()?.trim() ?: request.remoteAddr
        }
    }

    /**
     * Detects the device type of the client from the user-agent header.
     *
     * @param request The HTTP request containing the user-agent header.
     * @return A string representing the device type (`"android"`, `"ios"`, `"desktop"`, or `null`).
     */
    private fun detectDeviceType(request: HttpServletRequest): String? {
        val userAgent = request.getHeader("User-Agent")?.lowercase()
        return when {
            userAgent?.contains("android") == true -> "android"
            userAgent?.contains("iphone") == true || userAgent?.contains("ipad") == true -> "ios"
            userAgent?.contains("macintosh") == true || userAgent?.contains("mac os") == true || userAgent?.contains("windows") == true -> "desktop"
            else -> null
        }
    }

    /**
     * Retrieves the referring URL from the `Referer` header or `utm_source` parameter in the query string.
     *
     * @param request The HTTP request containing the referer information.
     * @return The referring URL or source, or `null` if unavailable.
     */
    private fun detectReferer(request: HttpServletRequest): String? {
        val referer = request.getHeader("Referer")?.lowercase()

        val queryString = request.queryString
        if (queryString != null) {
            val params = queryString.split("&")
            for (param in params) {
                val keyValue = param.split("=")
                if (keyValue.size == 2 && keyValue[0] == "utm_source") {
                    return keyValue[1].lowercase()
                }
            }
        }

        return referer?.lowercase()
    }

    /**
     * Extracts the user's language preference from the `Accept-Language` header.
     *
     * @param request The HTTP request containing the `Accept-Language` header.
     * @return The language code (first two characters of the language) or `"неизвестно"` if unavailable.
     */
    private fun detectUserLanguage(request: HttpServletRequest): String {
        return request.getHeader("Accept-Language")?.substring(0, 2)?.lowercase() ?: "неизвестно"
    }
}