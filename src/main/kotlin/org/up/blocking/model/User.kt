package org.up.blocking.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import org.up.coroutines.model.UserDto


@Entity(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
data class UserJpa(
        @field: Id @field: GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long,
        @field:Column(name = "user_name")
        val userName: String,
        val email: String,
        @field:Column(name = "email_verified")
        val emailVerified:Boolean,
        @field:Column(name = "avatar_url")
        val avatarUrl: String?) {

}

fun UserJpa.toDto(vararg threads:String = arrayOf(Thread.currentThread().toString())) =
        UserDto(id = id, userName = userName, email = email, avatarUrl = avatarUrl, threads = threads.toList())

