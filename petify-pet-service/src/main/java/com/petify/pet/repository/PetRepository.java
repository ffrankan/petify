package com.petify.pet.repository;

import com.petify.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    
    List<Pet> findByOwnerId(Long ownerId);
    
    List<Pet> findByOwnerIdAndStatus(Long ownerId, Integer status);
    
    Optional<Pet> findByIdAndOwnerId(Long id, Long ownerId);
    
    @Query("SELECT p FROM Pet p WHERE p.ownerId = :ownerId AND " +
           "(:name IS NULL OR p.name LIKE %:name%) AND " +
           "(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +
           "(:breedId IS NULL OR p.breedId = :breedId) AND " +
           "(:gender IS NULL OR p.gender = :gender) AND " +
           "(:status IS NULL OR p.status = :status)")
    List<Pet> findPetsByConditions(@Param("ownerId") Long ownerId,
                                   @Param("name") String name,
                                   @Param("categoryId") Long categoryId,
                                   @Param("breedId") Long breedId,
                                   @Param("gender") String gender,
                                   @Param("status") Integer status);
    
    long countByOwnerId(Long ownerId);
    
    boolean existsByMicrochipNumber(String microchipNumber);
    
    boolean existsByMicrochipNumberAndIdNot(String microchipNumber, Long id);
}