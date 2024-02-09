package org.up.reactor.repository;

import org.up.coroutines.model.User;
import org.up.coroutines.model.UserDto;

import java.util.List;

public class UserDtoBuilder {
    private User newUser;

    private UserDtoBuilder(User user) {
        this.newUser = user;
    }

    public static UserDtoBuilder from(User user) {
        User newUser = new User(null, user.getUserName(), user.getEmail(), user.getEmailVerified(), user.getAvatarUrl());
        return new UserDtoBuilder(newUser);
    }

    public UserDtoBuilder withAvatarUrl(String url) {
        newUser = new User(null, newUser.getUserName(), newUser.getEmail(), newUser.getEmailVerified(), url);
        return this;
    }

    public UserDtoBuilder withEmail(String email) {
        newUser = new User(null, newUser.getUserName(), email, true, newUser.getAvatarUrl());
        return this;
    }

    public UserDto buildDto() {
        return new UserDto(newUser.getId(), newUser.getUserName(), newUser.getEmail(), newUser.getEmailVerified(), newUser.getAvatarUrl(), List.of(Thread.currentThread().toString()));
    }
    public User build() {
        return newUser;
    }

}
