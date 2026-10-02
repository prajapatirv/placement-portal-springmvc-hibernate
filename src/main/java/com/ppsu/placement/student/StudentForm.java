package com.ppsu.placement.student;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record StudentForm(
        @NotBlank(message = "Name is required") @Size(max = 100) String name,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") @Size(max = 150) String email,
        @NotBlank(message = "Branch is required") @Size(max = 30) String branch,
        @NotNull(message = "CGPA is required")
        @DecimalMin(value = "0.0", message = "CGPA must be between 0 and 10")
        @DecimalMax(value = "10.0", message = "CGPA must be between 0 and 10") BigDecimal cgpa) {

    public static StudentForm empty() {
        return new StudentForm("", "", "", null);
    }
}
