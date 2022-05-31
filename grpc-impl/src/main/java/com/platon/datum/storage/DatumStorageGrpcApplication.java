package com.platon.datum.storage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.platon.datum.storage.dao")
public class DatumStorageGrpcApplication {
    public static void main(String[] args) {
        SpringApplication.run(DatumStorageGrpcApplication.class, args);
    }
}
