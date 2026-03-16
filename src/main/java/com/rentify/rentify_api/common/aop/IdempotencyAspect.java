package com.rentify.rentify_api.common.aop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rentify.rentify_api.common.exception.IdempotencyException;
import com.rentify.rentify_api.common.idempotency.IdempotencyKey;
import com.rentify.rentify_api.common.idempotency.IdempotencyKeyRepository;
import com.rentify.rentify_api.common.idempotency.IdempotencyStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.rentify.rentify_api.common.idempotency.Idempotent)")
    public Object checkIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String idempotencyKeyHeader = request.getHeader("Idempotency-Key");

        if (idempotencyKeyHeader == null || idempotencyKeyHeader.isBlank()) {
            throw new IdempotencyException("Idempotency-Key 헤더가 누락되었습니다.");
        }

        UUID idempotencyUuid;
        try {
            idempotencyUuid = UUID.fromString(idempotencyKeyHeader);
        } catch (IllegalArgumentException e) {
            throw new IdempotencyException("유효하지 않은 Idempotency-Key 형식입니다.");
        }

        Optional<IdempotencyKey> existKeyOpt = idempotencyKeyRepository.findById(idempotencyUuid);

        if (existKeyOpt.isPresent()) {
            IdempotencyKey existKey = existKeyOpt.get();

            if (existKey.getStatus() == IdempotencyStatus.SUCCESS) {
                return ResponseEntity.status(existKey.getResponseCode())
                    .body(existKey.getResponseBody());
            }

            throw new IdempotencyException("이전 요청이 아직 처리 중입니다. 잠시 후 결과를 확인해주세요.");
        }

        String requestUri = request.getRequestURI();
        String domain = requestUri.contains("/users") ? "USER" :
            requestUri.contains("/payments") ? "PAYMENT" :
            requestUri.contains("/posts") ? "POST" : "COMMON";

        IdempotencyKey newKey = IdempotencyKey.builder()
            .idempotencyKey(idempotencyUuid)
            .domain(domain)
            .status(IdempotencyStatus.PENDING)
            .build();

        try {
            newKey = idempotencyKeyRepository.saveAndFlush(newKey);
        } catch (DataIntegrityViolationException e) {
            throw new IdempotencyException("이전 요청이 아직 처리 중입니다. 잠시 후 결과를 확인해주세요.");
        }

        try {
            Object result = joinPoint.proceed();

            if (result instanceof ResponseEntity<?> responseEntity) {
                Object body = responseEntity.getBody();
                Map<String, Object> responseMap = objectMapper.convertValue(
                    body, new TypeReference<>() {}
                );
                int statusCode = responseEntity.getStatusCode().value();

                newKey.success(statusCode, responseMap);
            } else {
                newKey.success(200, objectMapper.convertValue(result, new TypeReference<>() {}));
            }

            idempotencyKeyRepository.save(newKey);
            return result;
        } catch (Exception e) {
            newKey.fail();
            idempotencyKeyRepository.save(newKey);
            throw e;
        }
    }
}
