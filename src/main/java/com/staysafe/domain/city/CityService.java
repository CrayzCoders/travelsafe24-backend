package com.staysafe.domain.city;

import com.staysafe.domain.district.District;
import com.staysafe.domain.district.DistrictRepository;

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
        City city = this.cityRepository.findByNameAndCountry("Hamburg", "Germany").orElseThrow();
        return this.districtRepository.findDistrictsByCity(city);
    }

    public void saveCity(City city) {
        cityRepository.save(city);
    }
}
