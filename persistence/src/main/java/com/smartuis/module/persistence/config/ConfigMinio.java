package com.smartuis.module.persistence.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigMinio {
    private String acces_key = "a1tng3PbhEXjobOdSh9O";
    private String secret_key = "GYf5MW8d9YyYlAl9XhUQ0UneWLpPv9Q0Bon6qOYf";

    @Bean
    MinioClient minioClient(){
        return MinioClient.builder()
                        .endpoint("http://127.0.0.1:9000/")
                        .credentials(acces_key, secret_key)
                        .build();
    }
}
