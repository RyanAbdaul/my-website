package com.ryan.personal_website_backend.aspect;

import com.ryan.personal_website_backend.annotation.RateLimit;
import com.ryan.personal_website_backend.annotation.RateLimitTier;
import com.ryan.personal_website_backend.exception.RateLimitExceededException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    @Value("${app.rate-limit.relaxed.requests:60}")
    private int relaxedRequests;

    @Value("${app.rate-limit.relaxed.window-seconds:60}")
    private int relaxedWindowSeconds;

    @Value("${app.rate-limit.strict.requests:10}")
    private int strictRequests;

    @Value("${app.rate-limit.strict.window-seconds:60}")
    private int strictWindowSeconds;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @PostConstruct
    void logConfig() {
        log.info("RateLimitAspect initialized: relaxed={}/{}s, strict={}/{}s",
                relaxedRequests, relaxedWindowSeconds, strictRequests, strictWindowSeconds);
    }

    @Around("@annotation(com.ryan.personal_website_backend.annotation.RateLimit)")
    public Object enforce(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = AnnotationUtils.findAnnotation(method, RateLimit.class);
        RateLimitTier tier = rateLimit != null ? rateLimit.value() : RateLimitTier.RELAXED;

        int limit = tier == RateLimitTier.STRICT ? strictRequests : relaxedRequests;
        int windowSeconds = tier == RateLimitTier.STRICT ? strictWindowSeconds : relaxedWindowSeconds;

        String key = clientKey() + ":" + tier.name();
        long now = System.currentTimeMillis();
        long windowMillis = windowSeconds * 1000L;

        Window window = windows.computeIfAbsent(key, ignored -> new Window(now));
        synchronized (window) {
            if (now - window.start >= windowMillis) {
                window.start = now;
                window.count.set(0);
            }
            if (window.count.incrementAndGet() > limit) {
                log.warn("Rate limit exceeded for {} ({})", key, tier);
                throw new RateLimitExceededException("Too many requests, please try again later.");
            }
        }

        return joinPoint.proceed();
    }

    private String clientKey() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        private long start;
        private final AtomicInteger count = new AtomicInteger();

        private Window(long start) {
            this.start = start;
        }
    }
}
