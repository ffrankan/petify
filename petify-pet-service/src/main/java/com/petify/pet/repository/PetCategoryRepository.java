package com.petify.pet.repository;

import com.petify.pet.entity.PetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetCategoryRepository extends JpaRepository<PetCategory, Long> {
    
    List<PetCategory> findByParentIdAndStatus(Long parentId, Integer status);
    
    List<PetCategory> findByParentIdIsNullAndStatus(Integer status);
    
    List<PetCategory> findByStatus(Integer status);
    
    List<PetCategory> findByStatusOrderBySortOrder(Integer status);
    
    @Query("SELECT pc FROM PetCategory pc WHERE pc.parentId = :parentId AND pc.status = :status ORDER BY pc.sortOrder")
    List<PetCategory> findChildrenByParentIdAndStatus(@Param("parentId") Long parentId, @Param("status") Integer status);
    
    @Query("SELECT pc FROM PetCategory pc WHERE pc.parentId IS NULL AND pc.status = :status ORDER BY pc.sortOrder")
    List<PetCategory> findRootCategoriesByStatus(@Param("status") Integer status);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);
    
    long countByParentId(Long parentId);
    
    // Additional methods for service implementation
    @Query("SELECT pc FROM PetCategory pc WHERE pc.status = 1 ORDER BY pc.sortOrder")
    List<PetCategory> findAllActive();
    
    @Query("SELECT pc FROM PetCategory pc WHERE pc.parentId = :parentId AND pc.status = 1 ORDER BY pc.sortOrder")
    List<PetCategory> findByParentIdAndActive(@Param("parentId") Long parentId);
}