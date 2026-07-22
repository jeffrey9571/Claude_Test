package com.koreanre.ifrs17.businessservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * IFRS17-BSL-SDD-001 v1.3 기준 Business Service Layer Phase 1 부트스트랩.
 * IFRS17 WAS 내부 모듈로 배포하며, 솔로몬 시스템에는 적용하지 않는다.
 */
@SpringBootApplication
public class BusinessServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusinessServiceApplication.class, args);
    }
}
