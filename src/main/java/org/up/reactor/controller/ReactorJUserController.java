package org.up.reactor.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.up.coroutines.model.AvatarDto;
import org.up.coroutines.model.User;
import org.up.coroutines.model.UserDto;
import org.up.reactor.repository.ReactorAvatarJService;
import org.up.reactor.repository.ReactorEnrollmentJService;
import org.up.reactor.repository.ReactorUserJRepository;
import org.up.reactor.repository.UserDtoBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;


@RestController
public class ReactorJUserController {

    private final ReactorUserJRepository reactorUserDao;
    private final ReactorAvatarJService reactorAvatarService;
    private final ReactorEnrollmentJService reactorEnrollmentJService;

    @Autowired
    public ReactorJUserController(ReactorUserJRepository reactorUserDao, ReactorAvatarJService reactorAvatarService, ReactorEnrollmentJService reactorEnrollmentJService) {
        this.reactorUserDao = reactorUserDao;
        this.reactorAvatarService = reactorAvatarService;
        this.reactorEnrollmentJService = reactorEnrollmentJService;
    }

    @GetMapping("/reactorj/users/{user-id}")
    @ResponseBody
    public Flux<User> getUser(@PathVariable("user-id") Long id) {
        return reactorUserDao.findById(id).flux();
    }


    @PostMapping(value = "/reactorj/users",consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    @Transactional
    public Mono<UserDto> storeUser(@RequestBody User user, @RequestParam("delay") Optional<Long> delay) {
        Mono<AvatarDto> avatarMono = reactorAvatarService.randomAvatar(delay.orElse(0L));
        Mono<Boolean> validEmailMono = reactorEnrollmentJService.verifyEmail(user.getEmail(), delay.orElse(0L));
        return Mono.zip(avatarMono, validEmailMono).flatMap(tuple -> {
                    if (!tuple.getT2())
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Email"));
                    else
                        return reactorUserDao.save(UserDtoBuilder.from(user)
                                .withEmail(user.getEmail())
                                .withAvatarUrl(tuple.getT1().getUrl())
                                .build()).map(stored ->
                                UserDtoBuilder.from(stored).buildDto());
                }
        );

    }

    @PostMapping(value = "/reactorj/users", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseBody
    public Mono<UserDto> storeUser_(User user, @RequestParam("delay") Optional<Long> delay) {
        Mono<AvatarDto> avatarMono = reactorAvatarService.randomAvatar(delay.orElse(0L));
        Mono<Boolean> validEmailMono = reactorEnrollmentJService.verifyEmail(user.getEmail(), delay.orElse(0L));
        return Mono.zip(avatarMono, validEmailMono).flatMap(tuple -> {
                    if (!tuple.getT2())
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Email"));
                    else
                        return reactorUserDao.save(UserDtoBuilder.from(user)
                                .withEmail(user.getEmail())
                                .withAvatarUrl(tuple.getT1().getUrl())
                                .build()).map(stored ->
                                UserDtoBuilder.from(stored).buildDto());
                }
        );

    }


    @GetMapping("/reactorj/{user-id}/sync-avatar")
    @ResponseBody
    @Transactional
    public Flux<User> syncAvatar(@PathVariable("user-id") Long id) {
        return reactorUserDao.findById(id)
                .flatMap(user ->
                        reactorAvatarService.randomAvatar()
                                .flatMap(avatar ->
                                        reactorUserDao.save(UserDtoBuilder
                                                .from(user)
                                                .withAvatarUrl(avatar.getUrl())
                                                .build())
                                )
                ).switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Unable to find user with id=$id")))
                .flux();
    }


}

