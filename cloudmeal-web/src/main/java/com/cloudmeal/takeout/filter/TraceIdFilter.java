package com.cloudmeal.takeout.filter;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * 链路追踪：为每个请求生成 traceId 写入 MDC，日志模板中可直接输出。
 *
 * <p>上游（网关 / Nginx）透传 {@code X-Trace-Id} 时沿用，便于全链路排查。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter implements Filter {

    public static final String TRACE_ID = "traceId";
    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String traceId = resolveTraceId(request);
        MDC.put(TRACE_ID, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }

    private String resolveTraceId(ServletRequest request) {
        if (request instanceof HttpServletRequest httpRequest) {
            String header = httpRequest.getHeader(TRACE_ID_HEADER);
            if (StringUtils.hasText(header)) {
                return header;
            }
        }
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
