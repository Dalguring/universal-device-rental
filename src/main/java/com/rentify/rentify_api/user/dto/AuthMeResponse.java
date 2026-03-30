package com.rentify.rentify_api.user.dto;

import com.rentify.rentify_api.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class AuthMeResponse {

    private Long userId;
    private String email;
    private String name;
    private String address;
    private Integer point;
    private Long postCount;
    private Long rentalCount;

    public AuthMeResponse(Long userId, String email, String name, String address,
                          Integer point, Long postCount, Long rentalCount) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.address = address;
        this.point = point;
        this.postCount = postCount;
        this.rentalCount = rentalCount;
    }

    public static AuthMeResponse from(User user, Long postCount, Long rentalCount) {
        return new AuthMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getAddress(),
                user.getPoint(),
                postCount,
                rentalCount
        );
    }

}