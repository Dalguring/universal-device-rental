package com.rentify.rentify_api.common.idempotency;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {

}
