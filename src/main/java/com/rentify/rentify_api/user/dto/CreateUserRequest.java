package com.rentify.rentify_api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CreateUserRequest {

    @NotBlank
    @Size(min = 2, max = 10)
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private String address;

    private String bank;

    @Pattern(
        regexp = "^[0-9]{10,20}$",
        message = "계좌번호는 숫자만 사용한 10 ~ 20자리여야 합니다."
    )
    private String account;

    @Pattern(
        regexp = "^01[0-9]{8,9}$",
        message = "휴대폰 번호는 숫자만 사용한 10~11자리여야 합니다."
    )
    private String phone;
}
