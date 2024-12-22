package ru.beetlink.backend.controllers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import ru.beetlink.backend.models.dto.request.link.LinkRequest
import ru.beetlink.backend.models.dto.response.link.LinkInfo
import ru.beetlink.backend.models.dto.response.link.LinkStatisticInfo
import ru.beetlink.backend.services.LinkService
import ru.beetlink.backend.utils.exception.InternalServerException
import java.time.LocalDate

/**
 * REST controller for managing links in the application.
 * Provides endpoints for creating, retrieving, updating, and deleting links,
 * as well as fetching statistics for a specific link.
 *
 * Base URL: `/api/v1/links`
 *
 * @property linkService The service layer for handling link-related operations.
 */
@RestController
@RequestMapping("/api/v1/links")
class LinkController(
    @Autowired private val linkService: LinkService
) {

    /**
     * Creates a new link for the authenticated user.
     *
     * @param linkRequest The request payload containing link details.
     * @param auth The authentication object containing the user's principal (user ID).
     * @return The information of the created link.
     */
    @PostMapping("/create")
    fun createLink(@RequestBody linkRequest: LinkRequest, auth: Authentication): LinkInfo {
        val userId = auth.principal as Long
        return linkService.createLink(userId, linkRequest)
    }

    /**
     * Retrieves a link by its ID for the authenticated user.
     *
     * @param linkId The ID of the link to retrieve.
     * @param auth The authentication object containing the user's principal (user ID).
     * @return The information of the retrieved link.
     */
    @GetMapping("/{linkId}")
    fun getLinkById(@PathVariable linkId: Long, auth: Authentication): LinkInfo {
        val userId = auth.principal as Long
        return linkService.getLinkById(userId, linkId)
    }

    /**
     * Updates an existing link for the authenticated user.
     *
     * @param linkId The ID of the link to update.
     * @param linkRequest The request payload containing updated link details.
     * @param auth The authentication object containing the user's principal (user ID).
     */
    @PutMapping("/{linkId}")
    fun updateLink(
        @PathVariable linkId: Long,
        @RequestBody linkRequest: LinkRequest,
        auth: Authentication
    ) {
        val userId = auth.principal as Long
        linkService.updateLink(userId, linkId, linkRequest)
    }

    /**
     * Deletes a link by its ID.
     *
     * @param linkId The ID of the link to delete.
     */
    @DeleteMapping("/{linkId}")
    fun deleteLink(@PathVariable linkId: Long) {
        linkService.deleteLink(linkId)
    }

    /**
     * Retrieves statistics for a specific link within the specified date range for the authenticated user.
     *
     * @param linkId The ID of the link for which to fetch statistics.
     * @param startDate The start date of the statistics period (in ISO-8601 format).
     * @param endDate The end date of the statistics period (in ISO-8601 format).
     * @param auth The authentication object containing the user's principal (user ID).
     * @return A list of statistics for the specified link.
     */
    @GetMapping("/{linkId}/statistics")
    fun getLinkStatistics(
        @PathVariable linkId: Long,
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        auth: Authentication
    ): List<LinkStatisticInfo> {
        val userId = auth.principal as Long
        return try {
            linkService.getLinkStatistics(userId, linkId, LocalDate.parse(startDate), LocalDate.parse(endDate))
        } catch (e: InternalServerException) {
            listOf()
        }
    }
}
