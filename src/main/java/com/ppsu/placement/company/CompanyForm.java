package com.ppsu.placement.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyForm(
        @NotBlank(message = "Name is required") @Size(max = 120) String name,
        @NotBlank(message = "City is required") @Size(max = 80) String city,
        @Size(max = 200) String website) {

    public static CompanyForm empty() {
        return new CompanyForm("", "", "");
    }
}
