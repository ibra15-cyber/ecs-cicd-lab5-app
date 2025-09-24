package com.ibra.simple_full_stack.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Slf4j
@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${s3.bucket.name}")
    private String bucketName;


    @Bean
    public S3Client s3Client() {
        log.info("Initializing S3 Client for region: {} and bucket: {}", awsRegion, bucketName);

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create());

        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        log.info("Initializing S3 Presigner for region: {}", awsRegion);

        return S3Presigner.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

//    @Bean
//    public S3Presigner s3Presigner() {
//        log.info("Initializing S3 Presigner for region: {}", awsRegion);
//
//        return S3Presigner.builder()
//                .region(Region.of(awsRegion))
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        AwsBasicCredentials.create(accessKey, secretKey))) // <-- Change to use credentials from properties
//                .build();
//    }


    //    @Bean
//    public S3Client s3Client() {
//        return S3Client.builder()
//                .region(Region.of(awsRegion))
//                .credentialsProvider(StaticCredentialsProvider.create(
//                        AwsBasicCredentials.create(accessKey, secretKey)))
//                .build();
//    }
    @Bean
    @Profile("local")
    public S3Client localS3Client() {
        // For local development with LocalStack or similar
        log.info("Using local S3 client configuration");

        return S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}