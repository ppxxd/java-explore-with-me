package ru.practicum.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitEndpointDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.exception.ValidationException;
import ru.practicum.server.mapper.StatsMapper;
import ru.practicum.server.model.Stats;
import ru.practicum.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final StatsRepository statsRepository;

    @Transactional
    @Override
    public void postHit(HitEndpointDto hit) {
        Stats stats = StatsMapper.fromDto(hit);
        log.info("Create new hit: {}", stats);
        statsRepository.save(stats);
        log.info("Hit was create");
    }


    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException(
                    String.format("Unexpected time interval: start %s; end %s", start, end));
        }

        log.info("Get stats by start [{}],\n end [{}],\n uris [{}],\n unique [{}]", start, end, uris, unique);
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statsRepository.getAllUniqueStats(start, end);
            } else {
                return statsRepository.getAllStats(start, end);
            }
        } else {
            if (unique) {
                return statsRepository.getUniqueStatsByUris(start, end, uris);
            } else {
                return statsRepository.getStatsByUris(start, end, uris);
            }
        }
    }

}
