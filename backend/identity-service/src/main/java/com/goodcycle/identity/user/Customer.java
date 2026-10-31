package com.goodcycle.identity.user;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String phone;

    @Column(nullable = false)
    private Instant createdAt;

    protected Customer() {}

    public Customer(String phone) {
        this.phone = phone;
        this.createdAt = Instant.now();
    }

    public String getPhone() {
        return phone;
    }
}
