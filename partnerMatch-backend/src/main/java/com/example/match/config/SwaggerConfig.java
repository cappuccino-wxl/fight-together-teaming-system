package com.example.match.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * swagger 配置
 */
@Configuration
@Profile({"dev","test"})
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MoMo API")
                        .version("1.0")
                        .description( "组队系统接口")
                        .termsOfService("https://github.com/cappuccino-wxl")
                        .license(new License().name("partnerMatch-backend 1.0")
                                .url("https://github.com/cappuccino-wxl")));
    }


}
