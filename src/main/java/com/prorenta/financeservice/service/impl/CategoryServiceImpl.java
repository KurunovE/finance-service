package com.prorenta.financeservice.service.impl;

import com.prorenta.financeservice.exception.CategoryNotFoundException;
import com.prorenta.financeservice.model.entity.Category;
import com.prorenta.financeservice.repository.CategoryRepository;
import com.prorenta.financeservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category findById(UUID id) {
        return categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Кактегория с id=" + id + " не найдена")
        );
    }
}
