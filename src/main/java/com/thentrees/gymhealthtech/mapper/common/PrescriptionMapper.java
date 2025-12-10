package com.thentrees.gymhealthtech.mapper.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thentrees.gymhealthtech.dto.request.CreateCustomPlanItemRequest;
import org.springframework.stereotype.Component;

/**
 * Utility mapper for converting prescription objects to/from JsonNode.
 * Used by PlanItem entity which stores prescription as JSONB.
 */
@Component
public class PrescriptionMapper {
  
  private final ObjectMapper objectMapper;

  public PrescriptionMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Convert PlanItemPrescription DTO to JsonNode for database storage
   */
  public JsonNode toJsonNode(CreateCustomPlanItemRequest.PlanItemPrescription prescription) {
    if (prescription == null) {
      return objectMapper.createObjectNode();
    }

    ObjectNode node = objectMapper.createObjectNode();
    node.put("sets", prescription.getSets());
    node.put("reps", prescription.getReps());

    if (prescription.getRestSeconds() != null) {
      node.put("restSeconds", prescription.getRestSeconds());
    }

    if (prescription.getWeightKg() != null) {
      node.put("weight", prescription.getWeightKg());
    }

    if (prescription.getRpe() != null) {
      node.put("rpe", prescription.getRpe());
    }

    return node;
  }

  /**
   * Convert Object (from AI service) to JsonNode
   */
  public JsonNode objectToJsonNode(Object prescriptionObject) {
    if (prescriptionObject == null) {
      return objectMapper.createObjectNode();
    }

    try {
      return objectMapper.valueToTree(prescriptionObject);
    } catch (Exception e) {
      // Return empty node if conversion fails
      return objectMapper.createObjectNode();
    }
  }

  /**
   * Convert simple prescription parameters to JsonNode
   */
  public JsonNode toJsonNode(Integer sets, Integer reps, Integer restSeconds) {
    ObjectNode node = objectMapper.createObjectNode();
    if (sets != null) {
      node.put("sets", sets);
    }
    if (reps != null) {
      node.put("reps", reps);
    }
    if (restSeconds != null) {
      node.put("restSeconds", restSeconds);
    }
    return node;
  }
}

