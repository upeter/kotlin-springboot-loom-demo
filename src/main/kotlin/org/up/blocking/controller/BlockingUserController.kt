package org.up.blocking.controller

import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.up.blocking.model.UserJpa
import org.up.blocking.model.toDto
import org.up.blocking.repository.BlockingAvatarService
import org.up.blocking.repository.BlockingEnrollmentService
import org.up.blocking.repository.BlockingUserDao
import org.up.coroutines.model.UserDto
import org.up.utils.VT
import org.up.utils.logger

@RestController
class BlockingUserController(
    private val blockingUserDao: BlockingUserDao,
    private val blockingAvatarService: BlockingAvatarService,
    private val blockingEnrollmentService: BlockingEnrollmentService
) {


    @GetMapping("/blocking/users/{user-id}")
    @ResponseBody
    fun getUser(@PathVariable("user-id") id: Long = 0): UserDto? =
        blockingUserDao.findByIdOrNull(id)?.toDto()

    @GetMapping("/blocking/users")
    @ResponseBody
    fun getUsers(): List<UserDto> =
        blockingUserDao.findAll().map { it.toDto() }


    /**
     * Check my talk to get detailed information why runBlocking for Spring MVC applications is an UNDESIRABLE solution.
     * See some remarks below why this is a problem.
     *
     * Instead, always use Coroutine enabled endpoints starting with the suspend keyword.
     */
    @PostMapping("/blocking/users", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Transactional
    fun storeUser_Do_Not_Use_Run_Blocking(@RequestBody user: UserJpa, @RequestParam(required = false) delay: Long? = null): UserDto {
        logger.info("Start storeUser") //IMPORTANT: MDC will be logged here, but not in the async blocks
        return runBlocking(Dispatchers.VT){
            val avatarUrl = async{
                //IMPORTANT: in here you don't have access to all ThreadLocal based attributed like: Spring's SecurityContext, MDC & Transaction,
                //which might result to unexpected behaviour.
                blockingAvatarService.randomAvatar(delay).url.also{
                    //MDC won't be logged
                    logger.info("Calling: avatarUrl")
                }
            }
            val emailVerified = async{
                //IMPORTANT: in here you don't have access to all ThreadLocal based attributed like: Spring's SecurityContext, MDC & Transaction,
                //which might result to unexpected behaviour.
                blockingEnrollmentService.verifyEmail(user.email, delay).also{
                    //MDC won't be logged
                    logger.info("Calling: emailVerified")
                }
            }
            blockingUserDao.save(user.copy(avatarUrl = avatarUrl.await(), emailVerified = emailVerified.await())).toDto()
        }
    }






    //required for performance test 'baton'
    @PostMapping("/blocking/users", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @ResponseBody
    @Transactional
    fun storeUser_(user: UserJpa, @RequestParam(required = false) delay: Long? = null): UserDto {
        return storeUser_Do_Not_Use_Run_Blocking(user, delay)
    }


}

