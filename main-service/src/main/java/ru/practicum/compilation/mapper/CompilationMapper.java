package ru.practicum.compilation.mapper;

import lombok.AllArgsConstructor;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;

import java.util.ArrayList;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CompilationMapper {

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
