package com.amalitech.photo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

	/**
	 * S3 client used to upload images. On Fargate the DefaultCredentialsProvider
	 * resolves the ECS task role automatically (no static keys), and traffic to
	 * S3 leaves the private subnets through the S3 gateway VPC endpoint.
	 */
	@Bean
	public S3Client s3Client(@Value("${app.aws.region}") String region) {
		return S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}
}
