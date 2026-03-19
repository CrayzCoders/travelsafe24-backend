package com.staysafe.services.city;

import com.staysafe.database.entities.City;
import com.staysafe.database.entities.District;
import com.staysafe.database.repositories.CityRepository;
import com.staysafe.database.repositories.DistrictRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class CityService {
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;

    public CityService(CityRepository cityRepository, DistrictRepository districtRepository) {
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
    }

    public City findOrCreate(String name, String country) {
        return this.cityRepository.findByNameAndCountry(name, country).orElseGet(
                () -> this.cityRepository.save(new City(name, country))
        );
    }

    public Optional<District> findDistrict(String name, City city) {
        return this.districtRepository.findByNameAndCity(name, city);
    }

    public List<District> getAllDistricts() {
        City city = this.cityRepository.findByNameAndCountry("Hamburg", "Germany").get();
        return this.districtRepository.findDistrictsByCity(city);
    }
}
