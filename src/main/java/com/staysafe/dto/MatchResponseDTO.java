package com.staysafe.dto;

import java.util.Map;

public class MatchResponseDTO {
    private final MatchResponseInfo infos;
    private final Map<String, DistrictDTO> districts;

    public MatchResponseDTO(MatchResponseInfo infos, Map<String, DistrictDTO> districts) {
        this.infos = infos;
        this.districts = districts;
    }

    public MatchResponseInfo getInfos() {
        return infos;
    }

    public Map<String, DistrictDTO> getDistricts() {
        return districts;
    }
}
