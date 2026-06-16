package com.amalitech.photo;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

	private final PhotoRepository repository;
	private final StorageService storage;
	private final String cloudFrontDomain;

	public PhotoController(PhotoRepository repository, StorageService storage,
			@Value("${app.cloudfront.domain}") String cloudFrontDomain) {
		this.repository = repository;
		this.storage = storage;
		this.cloudFrontDomain = cloudFrontDomain;
	}

	/** Gallery feed: every photo with its description and a CloudFront image URL. */
	@GetMapping
	public List<PhotoView> list() {
		return repository.findAllByOrderByCreatedAtDesc().stream()
				.map(this::toView)
				.toList();
	}

	/** Upload a new image with a description. Bytes go to S3, metadata to RDS. */
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<PhotoView> upload(
			@RequestParam("file") MultipartFile file,
			@RequestParam("description") String description) throws IOException {

		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
		}
		if (!StringUtils.hasText(description)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description is required");
		}

		String key = storage.store(file);
		Photo saved = repository.save(new Photo(description.trim(), key, file.getContentType()));
		return ResponseEntity.status(HttpStatus.CREATED).body(toView(saved));
	}

	private PhotoView toView(Photo photo) {
		return new PhotoView(
				photo.getId(),
				photo.getDescription(),
				"https://" + cloudFrontDomain + "/" + photo.getObjectKey(),
				photo.getCreatedAt());
	}

	/** Lightweight projection returned to the browser. */
	public record PhotoView(Long id, String description, String imageUrl, Instant createdAt) {
	}
}
