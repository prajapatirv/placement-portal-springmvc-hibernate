package com.ppsu.placement.application;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ApplyForm(@NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email) {}
