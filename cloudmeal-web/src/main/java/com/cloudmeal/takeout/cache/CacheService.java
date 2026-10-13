package com.cloudmeal.takeout.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Arrays;
import java.util.Set;

/**
 * 缓存读写统一入口。
 *
 * <p>把「怎么读写 Redis」收敛到一处，业务代码只关心 key 与过期时间，
 * 同时提供按前缀失效能力，避免出现「写库了但缓存还是旧数据」的一致性问题。</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        try {
            return (T) redisTemplate.opsForValue().get(key);
        } catch (Exception ex) {
            // 缓存故障不应阻断主流程：降级为直接查库
            log.warn("读取缓存失败，降级查询数据库：key={}, cause={}", key, ex.getMessage());
            return null;
        }
    }

    public void put(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception ex) {
            log.warn("写入缓存失败：key={}, cause={}", key, ex.getMessage());
        }
    }

    public void evict(String... keys) {
        try {
            if (keys.length > 0) {
                redisTemplate.delete(Arrays.asList(keys));
            }
        } catch (Exception ex) {
            log.warn("清理缓存失败：keys={}, cause={}", Arrays.toString(keys), ex.getMessage());
        }
    }

    /**
     * 按前缀批量失效。
     *
     * <p>中小规模数据使用 KEYS 足够；若 key 数量大，生产环境应改为 SCAN 游标分批删除。</p>
     */
    public void evictByPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (!CollectionUtils.isEmpty(keys)) {
                redisTemplate.delete(keys);
            }
        } catch (Exception ex) {
            log.warn("按前缀清理缓存失败：prefix={}, cause={}", prefix, ex.getMessage());
        }
    }
}
