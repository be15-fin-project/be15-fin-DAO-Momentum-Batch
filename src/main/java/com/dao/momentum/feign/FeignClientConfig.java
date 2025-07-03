package com.dao.momentum.feign;

import feign.RequestInterceptor;
import feign.Retryer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class FeignClientConfig {

    @Value("${batch.jwt-token}")
    private String batchJwtToken;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            if (batchJwtToken != null && !batchJwtToken.isBlank()) {
                requestTemplate.header("Authorization", "Bearer " + batchJwtToken);
            }
        };
    }

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(
                2000,                  // 초기 재시도 간격: 2초
                TimeUnit.SECONDS.toMillis(10),  // 최대 재시도 간격: 10초
                5                      // 최대 5회 시도
        );
    }
}
