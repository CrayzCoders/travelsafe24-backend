package com.staysafe.application.controller.matchingscore;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Response containing matching scores and metadata for all Hamburg districts")
public class MatchResponseDTO {

    @Schema(description = "Summary information about the scoring run")
    private final MatchResponseInfo infos;

    @Schema(description = "Map of district name to its matching score and per-criteria breakdown")
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
