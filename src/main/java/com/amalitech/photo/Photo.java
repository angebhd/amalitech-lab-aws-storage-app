package com.amalitech.photo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Photo metadata row. The image bytes live in Amazon S3 (served via
 * CloudFront); only the description and the S3 object key are persisted to
 * Amazon RDS PostgreSQL.
 */
@Entity
@Table(name = "photos")
public class Photo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 512)
	private String description;

	/** Key of the object in the S3 image bucket, e.g. {@code photos/<uuid>.jpg}. */
	@Column(name = "object_key", nullable = false, length = 512)
	private String objectKey;

	@Column(name = "content_type", length = 100)
	private String contentType;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	protected Photo() {
		// JPA
	}

	public Photo(String description, String objectKey, String contentType) {
		this.description = description;
		this.objectKey = objectKey;
		this.contentType = contentType;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public String getObjectKey() {
		return objectKey;
	}

	public String getContentType() {
		return contentType;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
