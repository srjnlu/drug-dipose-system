package com.goodcycle.disposal.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "disposal_requests")
public class DisposalRequest {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 16)
    private String customerPhone;

    @Column(nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false, length = 40)
    private String medicineType;

    @Column(nullable = false, length = 40)
    private String quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisposalMethod method;

    @Column(nullable = false, length = 120)
    private String location;

    @Column(length = 500)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private DisposalStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected DisposalRequest() {}

    public DisposalRequest(
            String customerPhone, String customerName, String medicineType,
            String quantity, DisposalMethod method, String location, String note) {
        this.id = UUID.randomUUID().toString();
        this.customerPhone = customerPhone;
        this.customerName = customerName;
        this.medicineType = medicineType;
        this.quantity = quantity;
        this.method = method;
        this.location = location;
        this.note = note;
        this.status = DisposalStatus.SUBMITTED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() { return id; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerName() { return customerName; }
    public String getMedicineType() { return medicineType; }
    public String getQuantity() { return quantity; }
    public DisposalMethod getMethod() { return method; }
    public String getLocation() { return location; }
    public String getNote() { return note; }
    public DisposalStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void updateStatus(DisposalStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
