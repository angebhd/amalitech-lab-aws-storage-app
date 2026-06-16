package com.amalitech.photo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

	/** Newest photos first, for the gallery view. */
	List<Photo> findAllByOrderByCreatedAtDesc();
}
