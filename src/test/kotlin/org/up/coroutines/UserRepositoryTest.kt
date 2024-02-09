package org.up.coroutines

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Transactional
import org.up.coroutines.model.User
import org.up.coroutines.repository.UserRepository


@SpringBootTest
@Transactional
@ExtendWith(SpringExtension::class)
class UserRepositoryTest @Autowired constructor(
    val userRepository: UserRepository
) {

    @Test
    fun `should insert new user and find it by username`():Unit = runBlocking{
        val newUser = User(null, "test insert", "joe@home.nl", false, null)
        val saveUser = userRepository.save(newUser)
        saveUser.id shouldNotBe null
        userRepository.findByUserName(saveUser.userName) shouldBe saveUser
    }
}