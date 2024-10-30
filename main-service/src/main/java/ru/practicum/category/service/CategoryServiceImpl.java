package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.ObjectNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ConflictException(String.format("Category %s already exists", newCategoryDto.getName()));
        }

        Category category = categoryRepository.save(CategoryMapper.fromDto(newCategoryDto));
        log.info("CATEGORY SAVED: {}", category);
        return CategoryMapper.toDto(category); //TODO CHECK ID
    }

    @Override
    public void deleteCategory(Long catId) {
        if (!eventRepository.findAllByCategoryId(catId).isEmpty()) {
            throw new ConflictException("You can't delete category with linked events");
        }

        checkCategoryExistsById(catId);
        categoryRepository.deleteById(catId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long catId, NewCategoryDto newCategoryDto) {
        Category category = checkCategoryExistsById(catId);
        Optional<Category> cat = categoryRepository.findByName(newCategoryDto.getName());

        if (cat.isPresent() && !category.getName().equals(newCategoryDto.getName())) {
            log.info(String.format("Category name: %s, new category name: %s", category.getName(), newCategoryDto.getName()));
            throw new ConflictException(String.format("Category %s already exists",
                    newCategoryDto.getName()));
        }

        category.setName(newCategoryDto.getName());
        return CategoryMapper.toDto(category);
    }

    @Override
    public List<CategoryDto> getGategories(int from, int size) {
        PageRequest pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable).stream().map(CategoryMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = checkCategoryExistsById(catId);
        return CategoryMapper.toDto(category);
    }

    @Override
    public Category checkCategoryExistsById(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(
                () -> new ObjectNotFoundException(String.format("Category with id=%s not found", catId))
        );
    }


}
