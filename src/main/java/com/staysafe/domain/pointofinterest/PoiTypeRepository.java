package com.staysafe.domain.pointofinterest;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PoiTypeRepository extends JpaRepository<PoiType, Long> {
    Optional<PoiType> findByName(String typeName);
}
