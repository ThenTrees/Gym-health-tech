package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Equipment;
import com.thentrees.gymhealthtech.model.Muscle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, String> {
  @Query("SELECT m FROM Equipment m WHERE m.code IN :codes")
  List<Muscle> findByCodes(@Param("codes") List<String> codes);

  @Query("SELECT m FROM Equipment m WHERE m.name LIKE %:name%")
  List<Equipment> findByName(@Param("name") String name);

  Optional<Equipment> findByCode(String codes);
}
