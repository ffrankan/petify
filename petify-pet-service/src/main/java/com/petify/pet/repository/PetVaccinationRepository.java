package com.petify.pet.repository;

import com.petify.pet.entity.Pet;
import com.petify.pet.entity.PetVaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PetVaccinationRepository extends JpaRepository<PetVaccination, Long> {
    
    List<PetVaccination> findByPetId(Long petId);
    
    List<PetVaccination> findByPetIdOrderByVaccinationDateDesc(Long petId);
    
    @Query("SELECT pv FROM PetVaccination pv WHERE pv.nextDueDate BETWEEN :fromDate AND :toDate")
    List<PetVaccination> findVaccinationsDueBetween(@Param("fromDate") LocalDate fromDate, 
                                                     @Param("toDate") LocalDate toDate);
    
    @Query("SELECT pv FROM PetVaccination pv WHERE pv.nextDueDate <= :date")
    List<PetVaccination> findOverdueVaccinations(@Param("date") LocalDate date);
    
    @Query("SELECT pv FROM PetVaccination pv WHERE pv.petId = :petId AND pv.vaccineName = :vaccineName")
    List<PetVaccination> findByPetIdAndVaccineName(@Param("petId") Long petId, 
                                                    @Param("vaccineName") String vaccineName);
    
    long countByPetId(Long petId);
    
    @Query("SELECT DISTINCT pv.vaccineName FROM PetVaccination pv ORDER BY pv.vaccineName")
    List<String> findDistinctVaccineNames();
    
    // Additional methods for service implementation
    @Query("SELECT pv FROM PetVaccination pv JOIN Pet p ON pv.petId = p.id " +
           "WHERE pv.petId = :petId AND p.ownerId = :ownerId ORDER BY pv.vaccinationDate DESC")
    List<PetVaccination> findByPetIdAndOwnerId(@Param("petId") Long petId, @Param("ownerId") Long ownerId);
    
    @Query("SELECT pv FROM PetVaccination pv JOIN Pet p ON pv.petId = p.id " +
           "WHERE pv.id = :vaccinationId AND pv.petId = :petId AND p.ownerId = :ownerId")
    PetVaccination findByIdAndPetIdAndOwnerId(@Param("vaccinationId") Long vaccinationId,
                                              @Param("petId") Long petId,
                                              @Param("ownerId") Long ownerId);
    
    @Query("SELECT pv FROM PetVaccination pv JOIN Pet p ON pv.petId = p.id " +
           "WHERE p.ownerId = :ownerId AND pv.nextDueDate <= :dueDate ORDER BY pv.nextDueDate")
    List<PetVaccination> findDueVaccinationsByOwnerId(@Param("ownerId") Long ownerId, @Param("dueDate") LocalDate dueDate);
}