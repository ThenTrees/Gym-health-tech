package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.TemplateDay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TemplateDayRepository extends JpaRepository<TemplateDay, UUID> {
}
