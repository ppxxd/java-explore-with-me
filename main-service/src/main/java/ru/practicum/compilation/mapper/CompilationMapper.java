package ru.practicum.compilation.mapper;

import lombok.AllArgsConstructor;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.service.EventService;

import java.util.ArrayList;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CompilationMapper {
    private static EventService eventService;

    public static Compilation fromNewDto(NewCompilationDto compilation) {
        if (compilation == null) {
            return null;
        }

        return Compilation.builder()
                .events(compilation.getEvents().isEmpty() ? new ArrayList<>() : compilation.getEvents()
                        .stream()
                        .map(eventId -> eventService.checkEventExistsById(eventId))
                        .collect(Collectors.toList()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public static CompilationDto toDto(Compilation compilation) {
        if (compilation == null) {
            return null;
        }

        return CompilationDto.builder()
                .id(compilation.getId())
                .events(compilation.getEvents().isEmpty() ? new ArrayList<>() : compilation.getEvents()
                        .stream()
                        .map(EventMapper::toShortDto)
                        .collect(Collectors.toList()))
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}
