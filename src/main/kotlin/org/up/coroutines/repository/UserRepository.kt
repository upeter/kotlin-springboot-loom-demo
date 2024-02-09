package org.up.coroutines.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.up.coroutines.model.AvatarDto
import org.up.coroutines.model.User
import reactor.core.publisher.Flux


@Repository
interface UserRepository : CoroutineCrudRepository<User, Long> {

    fun findById_GreaterThan(id: Long) : Flow<User>

    fun findById_GreaterThanAndEmailVerified(id: Long, verified: Boolean) : Flow<User>

    suspend fun findByUserName(userName: String) : User?
}


@Component
class AvatarService(@Value("\${remote.service.delay.ms}") val delayCfg: Long,
                    @Value("\${remote.service.url}") val baseUrl: String) {

    private val client by lazy { WebClient.create(baseUrl) }

    suspend fun randomAvatar(delay:Long? = null): AvatarDto =
            client.get()
                    .uri("/avatar?delay=${delay ?: delayCfg}")
                    .retrieve()
                    .awaitBody<AvatarDto>().also {
                        logger.debug("fetch random avatar...")
                    }

    companion object {
        val logger = LoggerFactory.getLogger(AvatarService::class.java)
    }

}

@Component
class EnrollmentService(@Value("\${remote.service.delay.ms}") val delayCfg: Long,
                        @Value("\${remote.service.url}") val baseUrl: String) {

    private val client by lazy { WebClient.create(baseUrl) }

    suspend fun verifyEmail(email: String, delay:Long? = null, verified:Boolean = true): Boolean =
            client.get()
                    .uri("/echo?email=$email&value=$verified&delay=${delay ?: delayCfg}")
                    .retrieve()
                    .awaitBody<String>()
                    .toBoolean().also {
                        logger.debug("verify email $email...")
                    }

    companion object {
        val logger = LoggerFactory.getLogger(EnrollmentService::class.java)
    }


}