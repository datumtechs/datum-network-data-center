package com.platon.rosettanet.storage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.platon.rosettanet.storage.dao")
public class RosettaNetStorageGrpcApplication {
    public static void main(String[] args) {
        SpringApplication.run(RosettaNetStorageGrpcApplication.class, args);
    }
}
