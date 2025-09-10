package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Equipment;
import com.thentrees.gymhealthtech.model.Muscle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentTypeRepository extends JpaRepository<Equipment, String> {
  @Query("SELECT m FROM Equipment m WHERE m.code IN :codes")
  List<Muscle> findByCodes(@Param("codes") List<String> codes);

}
