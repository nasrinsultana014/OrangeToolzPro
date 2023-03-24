package com.example.orangetoolzpro.domain;

import jakarta.persistence.*;
import lombok.Data;

//import java.util.regex.Pattern;
import jakarta.validation.constraints.Pattern;

import java.util.regex.Matcher;

@Data
@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String firstName;

    private String lastName;
    private String city;
    private String countryCode;
    private String zipCode;
    @Column(unique=true)
    @Pattern(regexp = "^((\\(\\d{3}\\))|\\d{3})[- ]?\\d{3}[- ]?\\d{4}$", message = "Phone number must be in proper format")
    private String phone;
    @Column(unique=true)
    @Pattern(regexp =  "^[A-Za-z0-9+_.-]+@(.+)$", message = "Email must be in proper format")
    private String email;
    private String ip;
}
