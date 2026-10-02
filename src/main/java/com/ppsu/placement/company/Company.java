package com.ppsu.placement.company;

import com.ppsu.placement.job.JobPosting;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String city;

    private String website;

    @OneToMany(mappedBy = "company")          // inverse side: the FK lives in job_posting
    private List<JobPosting> postings = new ArrayList<>();

    protected Company() {}                    // JPA needs a no-argument constructor

    public Company(String name, String city, String website) {
        this.name = name;
        this.city = city;
        this.website = website;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public List<JobPosting> getPostings() { return postings; }
}
