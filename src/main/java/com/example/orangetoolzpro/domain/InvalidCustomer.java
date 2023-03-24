package com.example.orangetoolzpro.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.regex.Matcher;

@Data
@Entity
public class InvalidCustomer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String firstName;
    private String lastName;
    private String city;
    private String countryCode;
    private String zipCode;
    private String phone;
    private String email;
    private String ip;

    public boolean isValidEmail(){
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        Matcher matcher = pattern.matcher(this.email);
        if(matcher.matches()) return true;
        else return false;
    }

    public boolean isValidPhone(){
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^((\\(\\d{3}\\))|\\d{3})[- ]?\\d{3}[- ]?\\d{4}$");
        Matcher matcher = pattern.matcher(this.phone);
        if(matcher.matches()) return true;
        else return false;
    }
}
