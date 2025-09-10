package com.thentrees.gymhealthtech.repository;

import com.thentrees.gymhealthtech.model.Muscle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MuscleRepository extends JpaRepository<Muscle, String> {
  List<Muscle> findAllByOrderByName();

  @Query("SELECT m FROM Muscle m WHERE m.code IN :codes")
  List<Muscle> findByCodes(@Param("codes") List<String> codes);

  Muscle findByCode(String code);
}
