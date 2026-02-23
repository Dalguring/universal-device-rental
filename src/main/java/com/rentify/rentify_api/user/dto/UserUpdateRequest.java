package com.rentify.rentify_api.user.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserUpdateRequest {

    @Nullable
    private String name;
    @Nullable
    @Email
    private String email;
    @Nullable
    private String address;
    @Nullable
    @Pattern(
        regexp = "^[0-9]{10,20}$",
        message = "계좌번호는 숫자만 사용한 10 ~ 20자리여야 합니다."
    )
    private String account;
}
