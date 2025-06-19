package com.petify.pet.repository;

import com.petify.pet.entity.PetBreed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetBreedRepository extends JpaRepository<PetBreed, Long> {
    
    List<PetBreed> findByCategoryIdAndStatus(Long categoryId, Integer status);
    
    List<PetBreed> findByStatus(Integer status);
    
    @Query("SELECT pb FROM PetBreed pb WHERE " +
           "(:categoryId IS NULL OR pb.categoryId = :categoryId) AND " +
           "(:name IS NULL OR pb.name LIKE %:name%) AND " +
           "(:sizeCategory IS NULL OR pb.sizeCategory = :sizeCategory) AND " +
           "(:originCountry IS NULL OR pb.originCountry = :originCountry) AND " +
           "pb.status = :status")
    List<PetBreed> findBreedsByConditions(@Param("categoryId") Long categoryId,
                                          @Param("name") String name,
                                          @Param("sizeCategory") String sizeCategory,
                                          @Param("originCountry") String originCountry,
                                          @Param("status") Integer status);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, Long id);
    
    long countByCategoryId(Long categoryId);
    
    List<String> findDistinctSizeCategoryByStatus(Integer status);
    
    List<String> findDistinctOriginCountryByStatus(Integer status);
    
    // Additional methods for service implementation
    @Query("SELECT pb FROM PetBreed pb WHERE pb.categoryId = :categoryId AND pb.status = 1 ORDER BY pb.name")
    List<PetBreed> findByCategoryIdAndActive(@Param("categoryId") Long categoryId);
    
    @Query("SELECT pb FROM PetBreed pb WHERE pb.name LIKE %:keyword% AND pb.status = 1 ORDER BY pb.name")
    List<PetBreed> findByKeywordWithLimit(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT pb FROM PetBreed pb WHERE " +
           "(:categoryId IS NULL OR pb.categoryId = :categoryId) AND " +
           "(:keyword IS NULL OR pb.name LIKE %:keyword%) AND " +
           "(:sizeCategory IS NULL OR pb.sizeCategory = :sizeCategory) AND " +
           "pb.status = 1 ORDER BY pb.name")
    List<PetBreed> findBySearchCriteria(@Param("categoryId") Long categoryId,
                                        @Param("keyword") String keyword,
                                        @Param("sizeCategory") String sizeCategory);
}