package com.platon.metis.storage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.platon.metis.storage.dao")
public class MetisStorageGrpcApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetisStorageGrpcApplication.class, args);
    }
}
