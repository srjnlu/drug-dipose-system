package com.goodcycle.disposal.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DisposalRequestRepository extends JpaRepository<DisposalRequest, String> {
    List<DisposalRequest> findAllByOrderByCreatedAtDesc();
    List<DisposalRequest> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);
    Optional<DisposalRequest> findByIdAndCustomerPhone(String id, String customerPhone);
}
