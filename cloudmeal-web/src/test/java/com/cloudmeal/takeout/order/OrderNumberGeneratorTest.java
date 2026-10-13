package com.cloudmeal.takeout.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单号生成器单元测试：重点验证高并发下不重复。
 */
class OrderNumberGeneratorTest {

    @Test
    @DisplayName("订单号长度为 18 位且全为数字")
    void format() {
        String number = new OrderNumberGenerator().next();
        assertEquals(18, number.length());
        assertTrue(number.matches("\\d{18}"));
    }

    @Test
    @DisplayName("连续生成 5000 个订单号互不重复")
    void uniqueUnderLoad() {
        OrderNumberGenerator generator = new OrderNumberGenerator();
        Set<String> numbers = new HashSet<>();
        for (int i = 0; i < 5000; i++) {
            numbers.add(generator.next());
        }
        assertEquals(5000, numbers.size());
    }
}
