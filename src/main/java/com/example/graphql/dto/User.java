package com.example.graphql.dto;

import com.example.graphql.UserRegistry;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record User(String name, int age, String countryOfResidence) {

    public static User toDTO(Optional<UserRegistry.User> maybeUser) {
        return new User(
                maybeUser.map(UserRegistry.User::name).orElse(""),
                maybeUser.map(UserRegistry.User::age).orElse(0),
                maybeUser.map(UserRegistry.User::countryOfResidence).orElse(""));
    }

    public static List<User> toDTO(List<UserRegistry.User> users) {
        return users.stream()
                .map(user -> new User(user.name(), user.age(), user.countryOfResidence()))
                .collect(Collectors.toList());
    }
}
