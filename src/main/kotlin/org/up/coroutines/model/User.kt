package org.up.coroutines.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
@JsonIgnoreProperties(ignoreUnknown = true)
data class User(@Id val id: Long? = null,
                val userName: String,
                val email: String,
                val emailVerified: Boolean = false,
                val avatarUrl: String? = null) {

}


data class AvatarDto @JsonCreator constructor(@JsonProperty("url") val url: String)


data class UserDto(
    val id: Long? = null,
    val userName: String,
                   val email: String,
                   val emailVerified: Boolean = false,
                   val avatarUrl: String? = null,
    val threads:List<String> = emptyList()
)


fun User.toDto(vararg threads:String = arrayOf(Thread.currentThread().toString())) =
    UserDto(id = id, userName = userName, email = email, avatarUrl = avatarUrl, threads = threads.toList())
