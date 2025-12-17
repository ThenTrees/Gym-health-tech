package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Post;
import com.thentrees.gymhealthtech.model.PostComment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {
  List<PostComment> findByPostAndParentCommentIsNull(Post post); // lấy comment gốc

  List<PostComment> findByParentComment(PostComment parent); // lấy reply theo parent
}
