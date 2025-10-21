package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Post;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, UUID> {}
