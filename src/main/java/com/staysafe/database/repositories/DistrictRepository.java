package com.staysafe.database.repositories;

import com.staysafe.database.entities.City;
import com.staysafe.database.entities.District;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District, Long> {
    Optional<District> findByNameAndCity(String name, City city);
}
