package com.rentify.rentify_api.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class ApiLoggingAspect {

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object printLog(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
            .currentRequestAttributes()).getRequest();
        String ip = getClientIp(request);
        String uri = request.getRequestURI();
        String httpMethod = request.getMethod();

        if (uri.startsWith("/api-docs")) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();

        Object[] args = joinPoint.getArgs();
        String[] parameterNames = signature.getParameterNames();
        Class<?>[] parameterTypes = signature.getParameterTypes();

        log.info("==== [API Request] {} {} (Client IP: {}) ====", httpMethod, uri, ip);
        log.info("Method: {}", methodName);

        for (int i = 0; i < args.length; i++) {
            String className = parameterTypes[i].getSimpleName();
            String paramName = parameterNames != null ? parameterNames[i] : "args" + i;
            Object arg = args[i];
            String argString;

            if (arg instanceof jakarta.servlet.http.HttpServletRequest ||
                arg instanceof jakarta.servlet.http.HttpServletResponse) {
                argString = arg.getClass().getSimpleName();
            }
            else if (arg != null && !isBasicType(arg.getClass())) {
                try {
                    argString = objectMapper.writeValueAsString(arg);
                } catch (Exception e) {
                    argString = arg.toString();
                }
            } else {
                argString = String.valueOf(arg);
            }

            argString = maskSensitiveData(argString);
            log.info("Parameter [{} {}]: {}", className, paramName, argString);
        }

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;
        String responseString;

        if (result == null) {
            responseString = "null";
        } else if (result instanceof ResponseEntity<?>) {
            Object body = ((ResponseEntity<?>) result).getBody();
            responseString = body != null ? trySerialize(body) : "null";
        } else if (isBasicType(result.getClass())) {
            responseString = String.valueOf(result);
        } else {
            responseString = trySerialize(result);
        }

        int maxLength = 1000;
        if (responseString.length() > maxLength) {
            responseString = responseString.substring(0, maxLength) +"...";
        }

        log.info("==== [API Response] {} {} ({}ms) ====", httpMethod, uri, executionTime);
        log.info("Response Data: {}\n", responseString);

        return result;
    }

    private String trySerialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.warn("Response 직렬화 실패: {}", e.getMessage());
            return object.toString();
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return (ip != null && ip.contains(",")) ? ip.split(",")[0] : ip;
    }

    private boolean isBasicType(Class<?> clazz) {
        return clazz.isPrimitive() ||
            clazz.getName().startsWith("java.lang") ||
            clazz.getName().startsWith("java.util") ||
            clazz.getName().startsWith("jakarta.servlet") ||
            clazz.getName().startsWith("org.springframework.web.multipart");
    }

    private String maskSensitiveData(String body) {
        if (body == null || "Empty Body".equals(body)) {
            return body;
        }
        return body.replaceAll("(\"(?i)[^\"]*password[^\"]*\"\\s*:\\s*\")[^\"]+(\")", "$1[PROTECTED]$2");
    }
}
