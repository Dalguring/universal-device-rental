package com.rentify.rentify_api.payment.repository;

import com.rentify.rentify_api.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
