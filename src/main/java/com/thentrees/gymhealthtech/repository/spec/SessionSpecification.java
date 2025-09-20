package com.thentrees.gymhealthtech.repository.spec;

import com.thentrees.gymhealthtech.common.SessionStatus;
import com.thentrees.gymhealthtech.model.Session;
import org.springframework.data.jpa.domain.Specification;

public class SessionSpecification {
  public static Specification<Session> hasKeyword(String keyword) {
    return (root, query, criteriaBuilder) -> {
      if (keyword == null || keyword.trim().isEmpty()) {
        return criteriaBuilder.conjunction();
      }

      String likeKeyword = "%" + keyword.toLowerCase() + "%";
      return criteriaBuilder.like(criteriaBuilder.lower(root.get("notes")), likeKeyword);
    };
  }

  public static Specification<Session> hasStatus(SessionStatus status) {
    return (root, query, criteriaBuilder) -> {
      if (status == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("status"), status);
    };
  }
}
