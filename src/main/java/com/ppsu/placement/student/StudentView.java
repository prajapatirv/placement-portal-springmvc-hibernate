package com.ppsu.placement.student;

import java.math.BigDecimal;

public record StudentView(Long id, String name, String email, String branch, BigDecimal cgpa) {}
