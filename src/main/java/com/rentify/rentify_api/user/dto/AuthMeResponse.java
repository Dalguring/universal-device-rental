package com.rentify.rentify_api.user.dto;

import com.rentify.rentify_api.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthMeResponse {

    private Long userId;
    private String email;
    private String name;

    public static AuthMeResponse from(User user) {
        return new AuthMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }
}