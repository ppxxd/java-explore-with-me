package ru.practicum.server.mapper;

import ru.practicum.dto.HitEndpointDto;
import ru.practicum.server.model.Stats;

public class StatsMapper {

    public static Stats fromDto(HitEndpointDto dto) {
        if (dto == null) {
            return null;
        }

        return Stats.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public static HitEndpointDto toDto(Stats stats) {
        if (stats == null) {
            return null;
        }

        return HitEndpointDto.builder()
                .app(stats.getApp())
                .uri(stats.getUri())
                .ip(stats.getIp())
                .timestamp(stats.getTimestamp())
                .build();
    }
}

