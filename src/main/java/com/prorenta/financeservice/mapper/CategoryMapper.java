package com.prorenta.financeservice.mapper;

import com.prorenta.financeservice.model.dto.CategoryResponseDto;
import com.prorenta.financeservice.model.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponseDto mapCategoryToCategoryResponseDto(Category category);
}
