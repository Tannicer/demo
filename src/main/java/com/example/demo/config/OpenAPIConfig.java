package com.example.demo.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("经验分享与审批系统 API 文档")
            .description("基于 Spring Boot + MyBatis 的银行经验分享审批平台接口文档，包含分享提交、审批、查询全流程接口")
            .version("v1.0.0")
            .contact(new Contact()
                .name("开发团队")
                .email("dev@example.com")
                .url("https://www.example.com"))
            .license(new License()
                .name("Apache 2.0")
                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
  }
}
