package ru.beetlink.backend.models.entity.link

import ru.beetlink.backend.models.entity.AbstractEntity
import jakarta.persistence.*
import ru.beetlink.backend.models.entity.user.User
import java.time.LocalDate

/**
 * Represents a link entity stored in the database.
 *
 * @property user The user associated with this link.
 * @property iosUrl The URL for iOS devices. Can be null.
 * @property androidUrl The URL for Android devices. Can be null.
 * @property desktopUrl The URL for desktop devices. Can be null.
 * @property defaultUrl The default URL. Must not be null.
 * @property description An optional description of the link. Can be null.
 * @property shortId A unique identifier for the link, used for short URLs. Can be null.
 * @property statistics A list of statistics associated with this link, such as usage data.
 */
@Entity
@Table(name = "links")
class Link(
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User,

    @Column(name = "ios_url")
    var iosUrl: String?,

    @Column(name = "android_url")
    var androidUrl: String?,

    @Column(name = "desktop_url")
    var desktopUrl: String?,

    @Column(name = "default_url", nullable = false)
    var defaultUrl: String,

    @Column(name = "description", length = 255)
    var description: String?,

    @Column(name = "short_id", unique = true)
    var shortId: String?,

    @OneToMany(mappedBy = "link", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var statistics: MutableList<LinkStatistic> = mutableListOf()

) : AbstractEntity() {

    /**
     * Adds a statistic to the list of statistics for this link and sets the relationship.
     *
     * @param statistic The statistic to be added.
     */
    fun addStatistic(statistic: LinkStatistic) {
        statistics.add(statistic)
        statistic.link = this
    }

    /**
     * Retrieves the daily statistics for the link within a given date range.
     *
     * @param startDate The start date of the range.
     * @param endDate The end date of the range.
     * @return A list of `LinkStatistic` objects that fall within the specified date range.
     */
    fun getDailyStatistics(startDate: LocalDate, endDate: LocalDate): List<LinkStatistic> {
        return statistics.filter {
            it.createdAt.toLocalDate().isAfter(startDate.minusDays(1)) &&
                    it.createdAt.toLocalDate().isBefore(endDate.plusDays(1))
        }
    }

    /**
     * Generates a short ID for the link based on its database ID.
     * The ID is converted to a base-36 representation using alphanumeric characters.
     */
    fun generateShortId() {
        val alphabet = "0123456789abcdefghijklmnopqrstuvwxyz"

        var number = this.id
        val base = 36
        val sb = StringBuilder()

        if (number == 0L) {
            shortId = alphabet[0].toString()
            return
        }

        while (number > 0) {
            val remainder = (number % base).toInt()
            sb.append(alphabet[remainder])
            number /= base
        }

        shortId = sb.reverse().toString()
    }
}