package com.prorenta.financeservice.repository;

import com.prorenta.financeservice.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    @Query("""
            SELECT COUNT(c.id)
            FROM Category c
            WHERE c.userId = :userId
                AND c.isDeleted = false
            """)
    int countLimitByUserId(@Param("userId") UUID userId);

    @Query("""
            SELECT c
            FROM Category c
            WHERE c.userId = :userId
                AND c.isDeleted = false
            """)
    List<Category> findAllByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("""
            UPDATE Category c
            SET c.isDeleted = true
            WHERE c.id = :categoryId
            """)
    void softRemoveCategoryById(@Param("categoryId") UUID categoryId);
}
