package com.prorenta.financeservice.service;

import com.prorenta.financeservice.model.entity.Category;

import java.util.UUID;

public interface CategoryService {
    Category findById(UUID id);
}
