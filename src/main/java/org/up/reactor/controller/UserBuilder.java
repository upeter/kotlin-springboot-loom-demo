package org.up.reactor.controller;

import org.up.blocking.model.UserJpa;

public class UserBuilder {
    private UserJpa newUser;

    private UserBuilder(UserJpa user) {
        this.newUser = user;
    }

    public static UserBuilder from(UserJpa user) {
        UserJpa newUser = new UserJpa(user.getId(), user.getUserName(), user.getEmail(), user.getEmailVerified(), user.getAvatarUrl());
        return new UserBuilder(newUser);
    }

    public UserBuilder withAvatarUrl(String url) {
        newUser = new UserJpa(newUser.getId(), newUser.getUserName(), newUser.getEmail(), newUser.getEmailVerified(), url);
        return this;
    }

    public UserBuilder withEmailVerified(boolean verified) {
        newUser = new UserJpa(newUser.getId(), newUser.getUserName(), newUser.getEmail(), verified, newUser.getAvatarUrl());
        return this;
    }

    public UserJpa build() {
        return newUser;
    }

}
