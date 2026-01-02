package com.rentify.rentify_api.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private final Long id;
    private final String name;
    private final String email;
    private final String address;
    private final String bank;
    private final String account;
    private final String phone;
}
