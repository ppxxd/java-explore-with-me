package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ObjectNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.fromNewDto(newCompilationDto);

        compilationRepository.save(compilation);

        return CompilationMapper.toDto(compilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        checkCompilationExistsById(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = checkCompilationExistsById(compId);

        if (updateCompilationRequest.getEvents() != null) {
            compilation.setEvents(eventRepository.findAllByIdIn(updateCompilationRequest.getEvents()));
        }
        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getTitle() != null) {
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        compilationRepository.save(compilation);

        return CompilationMapper.toDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, PageRequest pageable) {
        if (pinned != null) {
            return compilationRepository.findAllByPinned(pinned, pageable)
                    .stream()
                    .map(CompilationMapper::toDto)
                    .collect(Collectors.toList());
        }
        return compilationRepository.findAll(pageable)
                .stream()
                .map(CompilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        return CompilationMapper.toDto(checkCompilationExistsById(compId));
    }

    private Compilation checkCompilationExistsById(Long compId) {
        return compilationRepository.findById(compId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Compilation with id=%s was not found", compId)
                ));
    }
}
