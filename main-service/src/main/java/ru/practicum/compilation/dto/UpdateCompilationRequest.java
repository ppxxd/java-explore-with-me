package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class UpdateCompilationRequest {
    private List<Long> events;
    @Builder.Default
    private Boolean pinned = false;
    @Size(min = 1, max = 50)
    private String title;
}
