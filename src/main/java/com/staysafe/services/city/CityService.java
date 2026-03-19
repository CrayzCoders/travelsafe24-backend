package com.staysafe.services.city;

import com.staysafe.database.entities.City;
import com.staysafe.database.repositories.CityRepository;
import org.springframework.stereotype.Service;

@Service
public class CityService {
    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public City findOrCreate(String name, String country) {
        return this.cityRepository.findByNameAndCountry(name, country).orElseGet(
                () -> this.cityRepository.save(new City(name, country))
        );
    }
}
