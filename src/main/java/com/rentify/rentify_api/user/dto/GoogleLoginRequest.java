package com.rentify.rentify_api.user.dto;

import jakarta.annotation.Nonnull;

public record GoogleLoginRequest(@Nonnull String idToken) {}
