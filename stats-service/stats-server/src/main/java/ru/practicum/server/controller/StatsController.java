package ru.practicum.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.HitEndpointDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void postHit(@Valid @RequestBody HitEndpointDto hit) {
        log.info("Call postHit endpoint.");
        statsService.postHit(hit);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStatsDto>> getStats(@RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime start,
                                                    @RequestParam @DateTimeFormat(pattern = FORMAT) LocalDateTime end,
                                                    @RequestParam(required = false) List<String> uris,
                                                    @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Call getStats endpoint.");
        return new ResponseEntity<>(statsService.getStats(start, end, uris, unique), HttpStatus.OK);
    }
}
