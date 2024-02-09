package org.up.coroutines.controller

import jakarta.transaction.Transactional
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.up.blocking.repository.BlockingAvatarService
import org.up.blocking.repository.BlockingEnrollmentService
import org.up.coroutines.model.User
import org.up.coroutines.model.UserDto
import org.up.coroutines.model.toDto
import org.up.coroutines.repository.AvatarService
import org.up.coroutines.repository.EnrollmentService
import org.up.coroutines.repository.UserRepository
import org.up.utils.VT
import java.util.concurrent.atomic.AtomicLong

@RestController
class CoroutineUserController(
    private val userRepository: UserRepository,
    private val avatarService: AvatarService,
    private val enrollmentService: EnrollmentService,
    private val blockingAvatarService: BlockingAvatarService,
    private val blockingEnrollmentService: BlockingEnrollmentService

) {


    @GetMapping("/coroutines/users")
    @ResponseBody
    suspend fun getUsers(): Flow<UserDto> =
            userRepository.findAll().map{it.toDto()}


    @GetMapping("/coroutines/users/{user-id}")
    @ResponseBody
    suspend fun getUser(@PathVariable("user-id") id: Long = 0): UserDto? =
            userRepository.findById(id)?.toDto()


    @PostMapping("/coroutines/users", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Transactional
    suspend fun storeUser(@RequestBody user: User, @RequestParam(required = false) delay:Long? = null): UserDto? = coroutineScope{
        val emailVerified = async { enrollmentService.verifyEmail(user.email,  delay) }
        val avatarUrl = async { avatarService.randomAvatar(delay).url }
        userRepository.save(user.copy(id = null,avatarUrl = avatarUrl.await(), emailVerified = emailVerified.await())).toDto()
    }


    /**
     * Try to avoid doing blocking calls using Coroutines. Always try to use an async library instead, which makes VirtualThreads obsolete.
     * So {@link CoroutineUserController#storeUser()} is always the preferred solution.
     * But if you have to use a blocking API, then the below (using withContext(Dispatchers.VT){...} is the right way to go.
     */
    @PostMapping("/coroutines/users/blocking", consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseBody
    @Transactional
    suspend fun storeUserBlocking_Try_To_Avoid_This_Approach(@RequestBody user: User, @RequestParam(required = false) delay:Long? = null): UserDto? = withContext(Dispatchers.VT){
        val emailVerified = async { blockingEnrollmentService.verifyEmail(user.email,  delay) }
        val avatarUrl = async { blockingAvatarService.randomAvatar(delay).url }
        userRepository.save(user.copy(id = null,avatarUrl = avatarUrl.await(), emailVerified = emailVerified.await())).toDto()
    }



    //endpoint needed for performance test with 'baton' cli
    @PostMapping("/coroutines/users", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @ResponseBody
    @Transactional
    suspend fun storeUser_( user: User, @RequestParam(required = false) delay:Long? = null): UserDto? = storeUser(user, delay)










    @GetMapping("/coroutines/user")
    @ResponseBody
    suspend fun userByName(@RequestParam("userName") userName: String): UserDto? =
        userRepository.findByUserName(userName)?.toDto()

    @GetMapping("/coroutines/users/{user-id}/sync-avatar")
    @ResponseBody
    @Transactional
    suspend fun syncAvatar(@PathVariable("user-id") id: Long = 0): UserDto =
            userRepository.findById(id)?.let {
                val avatar = avatarService.randomAvatar()
                userRepository.save(it.copy(avatarUrl = avatar.url)).toDto()
            } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find user with id=$id")




















    @GetMapping("/coroutines/infinite")
    fun infiniteFlow(): Flow<String>  = flow {
        generateSequence(0){it + 1}.forEach {
            emit(it.toString() + " \n")
        }
    }


    @GetMapping("/coroutines/infinite/sse", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sseFlow(): Flow<ServerSentEvent<String>>  = flow {
        generateSequence(0){it + 1}.forEach {
            emit(ServerSentEvent.builder<String>().event("hello-sse-event").id(it.toString()).data("Your lucky number is $it").build())
            delay(500L)
        }
    }


    private val channel = BroadcastChannel<UserAddedNotification>(Channel.CONFLATED)




    data class UserAddedNotification(val verified:Int = 0, val nonVerified:Int = 0) {
        fun has(filterVerified:Boolean) = (if(filterVerified) verified else nonVerified) > 0
    }

    val lastId = AtomicLong(0)
    @Scheduled(fixedRate = 1000)
    fun singlePoller() = runBlocking{
        userRepository.findById_GreaterThan(lastId.get()).toList().partition { it.emailVerified }.also {(verified, notVerified) ->
            channel.send(UserAddedNotification(verified = verified.size, nonVerified = notVerified.size))
            lastId.set((verified + notVerified).map { it.id ?: 0 }.max() ?: 0)
        }
    }



    @GetMapping("/coroutines/users/stream0", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    suspend fun userFlow0(): Flow<User> = userRepository.findAll()


    @GetMapping("/coroutines/users/stream1", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    suspend fun userFlow1(@RequestParam("offset") offsetId: Long = 0): Flow<User> = flow {
        var latestId = offsetId
        suspend fun take() = userRepository.findById_GreaterThan(latestId).collect { user ->
            emit(user).also { latestId = user.id!! }
        }
        while (true) {
            delay(1000)
            take()
        }
    }

    @GetMapping("/coroutines/users/stream2", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    suspend fun userFlow2(@RequestParam("offset") offsetId: Long = 0): Flow<User> = flow {
        var latestId = offsetId
        suspend fun take() = userRepository.findById_GreaterThan(latestId).collect { user ->
            emit(user).also { latestId = user.id!! }
        }
        take()
        channel.openSubscription().consumeAsFlow().collect {
            take()
        }
    }


    @GetMapping("/coroutines/users/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    suspend fun userFlow(@RequestParam("offset") offsetId: Long? = 0, @RequestParam("verified") filterVerified: Boolean = true): Flow<User> = flow {
        var lastId = offsetId ?: 0
        suspend fun take() = userRepository.findById_GreaterThanAndEmailVerified(lastId, filterVerified).collect { user ->
            emit(user).also { lastId = user.id!! }
        }
        take()
        channel.openSubscription().consumeAsFlow().filter { it.has(filterVerified) }.collect {
            take()
        }
    }






}
