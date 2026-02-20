package com.rentify.rentify_api.user.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordUpdateRequest(
    @NotBlank String currentPassword,
    @NotBlank String newPassword
) {}