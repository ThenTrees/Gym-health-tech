package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.TemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TemplateItemRepository extends JpaRepository<TemplateItem, UUID> {
}
