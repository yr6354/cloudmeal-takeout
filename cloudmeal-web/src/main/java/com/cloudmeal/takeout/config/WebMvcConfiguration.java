package com.cloudmeal.takeout.config;

import com.cloudmeal.takeout.constant.JwtClaimsConstant;
import com.cloudmeal.takeout.interceptor.JwtAuthInterceptor;
import com.cloudmeal.takeout.json.JacksonObjectMapper;
import com.cloudmeal.takeout.properties.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web 层统一配置。
 *
 * <p>注意这里实现的是 {@link WebMvcConfigurer}，而不是继承 {@code WebMvcConfigurationSupport}：
 * 后者会关闭 Spring Boot 对 MVC 的自动配置，导致静态资源、错误处理等默认能力失效。</p>
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

    private final JwtProperties jwtProperties;

    /**
     * 注册管理端 / 用户端两套 JWT 拦截器（同一实现，不同参数）。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        JwtAuthInterceptor adminInterceptor = new JwtAuthInterceptor(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTokenName(),
                JwtClaimsConstant.EMP_ID);
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");

        JwtAuthInterceptor userInterceptor = new JwtAuthInterceptor(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTokenName(),
                JwtClaimsConstant.USER_ID);
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/user/**")
                .excludePathPatterns("/user/user/login", "/user/shop/status");

        log.info("已注册 JWT 鉴权拦截器：admin={}, user={}",
                jwtProperties.getAdminTokenName(), jwtProperties.getUserTokenName());
    }

    /** 开发期放开跨域，生产环境交由 Nginx / 网关统一处理。 */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /** 接口文档静态资源 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /** 覆盖默认 JSON 转换器：统一日期格式、忽略未知字段。 */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0, converter);
    }
}
