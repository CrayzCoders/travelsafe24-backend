package com.staysafe.services.area;

import com.staysafe.config.AppConfig;
import org.springframework.stereotype.Service;

@Service
public class AreaService {
    private final AppConfig appConfig;

    public AreaService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public boolean isValidArea(String area) {
        return this.appConfig.getAreas().contains(area);
    }
}
