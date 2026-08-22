package com.spring.spotlog_api.global.lock;

import com.spring.spotlog_api.global.exception.CustomException;
import com.spring.spotlog_api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {
    private static final String REDISSON_LOCK_PREFIX = "lock:";

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String key = REDISSON_LOCK_PREFIX + parseKey(distributedLock.key(), joinPoint);
        RLock rLock = redissonClient.getLock(key);

        boolean acquired;
        try {
            // leaseTime을 주지 않으므로 Watchdog이 자동으로 TTL을 연장해줌 (기본 30초 주기)
            acquired = rLock.tryLock(distributedLock.waitTime(), distributedLock.timeUnit());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
        }

        if (!acquired) {
            throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
        }

        try {
            // 별도 트랜잭션(REQUIRES_NEW)으로 실행 → 이 안에서 커밋까지 완료된 뒤에야 아래 finally에서 락 해제
            return aopForTransaction.proceed(joinPoint);
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    private String parseKey(String keyExpression, ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] parameterNames = new DefaultParameterNameDiscoverer().getParameterNames(method);

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}