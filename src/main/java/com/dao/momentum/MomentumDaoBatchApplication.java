package com.dao.momentum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.dao.momentum.feign")
@EnableScheduling
public class MomentumDaoBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MomentumDaoBatchApplication.class, args);
    }
}
