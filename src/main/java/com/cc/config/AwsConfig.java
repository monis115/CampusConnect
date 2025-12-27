package com.cc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS Configuration for S3 integration.
 * Uses DefaultCredentialsProvider which checks:
 * 1. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
 * 2. Java system properties
 * 3. IAM role (when running on EC2/ECS)
 */
@Configuration
public class AwsConfig {

    @Value("${aws.region:ap-south-1}")
    private String region;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Bean
    public S3Client s3Client() {
        if (!s3Enabled) {
            return null; // Return null when S3 is disabled (local development)
        }
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        if (!s3Enabled) {
            return null;
        }
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
