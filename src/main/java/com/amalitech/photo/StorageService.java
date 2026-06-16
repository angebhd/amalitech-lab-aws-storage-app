package com.amalitech.photo;

import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Stores uploaded image bytes in the private S3 bucket. The bucket blocks all
 * public access; images are reachable only through the CloudFront distribution
 * (Origin Access Control), so no public-read ACL is ever set here.
 */
@Service
public class StorageService {

	private final S3Client s3;
	private final String bucket;

	public StorageService(S3Client s3, @Value("${app.s3.bucket}") String bucket) {
		this.s3 = s3;
		this.bucket = bucket;
	}

	/** Uploads the file under a unique {@code photos/<uuid>.<ext>} key and returns that key. */
	public String store(MultipartFile file) throws IOException {
		String key = "photos/" + UUID.randomUUID() + extensionOf(file.getOriginalFilename());

		PutObjectRequest request = PutObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.contentType(file.getContentType())
				.build();

		s3.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
		return key;
	}

	private static String extensionOf(String filename) {
		if (filename == null) {
			return "";
		}
		int dot = filename.lastIndexOf('.');
		return dot >= 0 ? filename.substring(dot).toLowerCase() : "";
	}
}
