package com.staysafe.domain.amenity;

import com.staysafe.application.config.AppConfig;
import org.springframework.stereotype.Service;

@Service
public class AmenityService {
    private final AppConfig appConfig;

    public AmenityService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public boolean isValidAmenity(String amenity) {
        return this.appConfig.getAmenities().contains(amenity);
    }
}
