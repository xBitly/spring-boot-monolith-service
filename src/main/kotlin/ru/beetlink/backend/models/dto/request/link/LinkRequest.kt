package ru.beetlink.backend.models.dto.request.link

import ru.beetlink.backend.models.entity.link.Link
import ru.beetlink.backend.models.entity.user.User

/**
 * Represents the input data for creating or updating a link.
 *
 * @property iosUrl The URL to be used on iOS devices. Can be null.
 * @property androidUrl The URL to be used on Android devices. Can be null.
 * @property desktopUrl The URL to be used on desktop devices. Can be null.
 * @property defaultUrl The default URL to be used when no platform-specific URL is available. Must not be null.
 * @property description An optional description of the link. Can be null.
 */
data class LinkRequest(
    val iosUrl: String?,
    val androidUrl: String?,
    val desktopUrl: String?,
    val defaultUrl: String,
    val description: String?
)

/**
 * Converts the LinkRequest object to a Link entity.
 *
 * @param user The user associated with the link.
 * @return A Link entity populated with data from the LinkRequest and the provided user.
 */
fun LinkRequest.toEntity(user: User): Link {
    return Link(
        user = user,
        iosUrl = iosUrl,
        androidUrl = androidUrl,
        desktopUrl = desktopUrl,
        defaultUrl = defaultUrl,
        shortId = null,
        description = description
    )
}


