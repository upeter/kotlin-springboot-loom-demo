package org.up.reactor.controller;

import jakarta.transaction.Transactional;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.up.blocking.model.UserJpa;
import org.up.blocking.repository.BlockingAvatarService;
import org.up.blocking.repository.BlockingEnrollmentService;
import org.up.blocking.repository.BlockingUserDao;
import org.up.coroutines.model.UserDto;
import org.up.reactor.repository.UserDtoBuilder;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
public class LoomJUserController {

    private final BlockingUserDao blockingUserDao;
    private final BlockingAvatarService blockingAvatarService;
    private final BlockingEnrollmentService blockingEnrollmentService;

    @Autowired
    public LoomJUserController(BlockingUserDao blockingUserDao, BlockingAvatarService blockingAvatarService, BlockingEnrollmentService blockingEnrollmentService) {
        this.blockingUserDao = blockingUserDao;
        this.blockingAvatarService = blockingAvatarService;
        this.blockingEnrollmentService = blockingEnrollmentService;
    }


    @PostMapping(name = "/loomj/users", value = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    @Transactional
    public UserJpa storeUser(@RequestBody UserJpa user, @RequestParam(required = false) Optional<Long> delay) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var emailVerified = scope.fork(() ->
                    blockingEnrollmentService.verifyEmail(user.getEmail(), delay.orElse(0L))
            );
            var avatarUrl = scope.fork(() ->
                    blockingAvatarService.randomAvatar(delay.orElse(0L))
            );
            scope.join();
            scope.throwIfFailed();
            return blockingUserDao.save(UserBuilder.from(user)
                    .withEmailVerified(emailVerified.get())
                    .withAvatarUrl(avatarUrl.get().getUrl())
                    .build());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
