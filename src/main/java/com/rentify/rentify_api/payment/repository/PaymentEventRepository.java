package com.rentify.rentify_api.payment.repository;

import com.rentify.rentify_api.payment.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentEventRepository extends JpaRepository<PaymentEvent, Long> {

}
