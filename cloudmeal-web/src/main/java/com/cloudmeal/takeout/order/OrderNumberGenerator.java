package com.cloudmeal.takeout.order;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单号生成器。
 *
 * <p>此前直接用 {@code System.currentTimeMillis()}，同一毫秒内并发下单会得到相同订单号。
 * 这里改为「时间戳(yyyyMMddHHmmss) + 4 位进程内自增序号」，在保持可读、可用于对账的前提下规避碰撞。</p>
 */
@Component
public class OrderNumberGenerator {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int SEQUENCE_MODULUS = 10_000;

    private final AtomicInteger sequence = new AtomicInteger(new SecureRandom().nextInt(SEQUENCE_MODULUS));

    public String next() {
        int current = sequence.updateAndGet(value -> (value + 1) % SEQUENCE_MODULUS);
        return LocalDateTime.now().format(TIMESTAMP) + String.format("%04d", current);
    }
}
