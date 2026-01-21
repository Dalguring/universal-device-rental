package com.rentify.rentify_api.image.repository;

import com.rentify.rentify_api.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
