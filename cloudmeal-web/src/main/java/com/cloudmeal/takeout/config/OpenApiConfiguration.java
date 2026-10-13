package com.cloudmeal.takeout.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档元信息。
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI cloudMealOpenApi() {
        return new OpenAPI().info(new Info()
                .title("云膳外卖接口文档")
                .version("1.0.0")
                .description("管理端（/admin/**）面向商家员工，用户端（/user/**）面向小程序消费者。")
                .license(new License().name("Apache License 2.0")));
    }
}
