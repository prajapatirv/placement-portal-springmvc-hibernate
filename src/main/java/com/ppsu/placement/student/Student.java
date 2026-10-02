package com.ppsu.placement.student;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String branch;

    @Column(nullable = false)
    private BigDecimal cgpa;

    protected Student() {}

    public Student(String name, String email, String branch, BigDecimal cgpa) {
        this.name = name;
        this.email = email;
        this.branch = branch;
        this.cgpa = cgpa;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public BigDecimal getCgpa() { return cgpa; }
    public void setCgpa(BigDecimal cgpa) { this.cgpa = cgpa; }
}
