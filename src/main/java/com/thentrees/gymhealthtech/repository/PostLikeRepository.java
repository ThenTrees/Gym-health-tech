package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Post;
import com.thentrees.gymhealthtech.model.PostLike;
import com.thentrees.gymhealthtech.model.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
  PostLike findByPostAndUser(Post post, User user);
}
