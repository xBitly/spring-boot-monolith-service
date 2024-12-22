package ru.beetlink.backend.models.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import ru.beetlink.backend.models.entity.link.Link
import ru.beetlink.backend.models.entity.user.User

/**
 * Repository interface for managing `Link` entities in the database.
 * Extends the Spring Data JPA `JpaRepository` to provide CRUD operations and custom query methods.
 */
@Repository
interface LinkRepository : JpaRepository<Link, Long> {

    /**
     * Retrieves a `Link` entity by its ID.
     *
     * @param id The ID of the link to retrieve.
     * @return The `Link` entity with the given ID, or `null` if no such link exists.
     */
    fun getLinkById(id: Long): Link?

    /**
     * Retrieves a `Link` entity by its short ID.
     *
     * @param shortId The short ID of the link to retrieve.
     * @return The `Link` entity with the given short ID, or `null` if no such link exists.
     */
    fun getLinkByShortId(shortId: String): Link?

    /**
     * Removes a `Link` entity from the database by its ID.
     *
     * @param id The ID of the link to remove.
     */
    fun removeLinkById(id: Long)
}