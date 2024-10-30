package ru.practicum.category.mapper;


import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.model.Category;


public class CategoryMapper {

    public static Category fromDto(NewCategoryDto dto) {
        if (dto == null) {
            return null;
        }

        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public static Category fromFullDto(CategoryDto dto) {
        if (dto == null) {
            return null;
        }

        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }

    public static CategoryDto toDto(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
