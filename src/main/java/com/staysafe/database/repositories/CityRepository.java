package com.staysafe.database.repositories;

import com.staysafe.database.entities.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameAndCountry(String name, String country);
}
