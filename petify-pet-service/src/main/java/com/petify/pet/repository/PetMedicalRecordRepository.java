package com.petify.pet.repository;

import com.petify.pet.entity.PetMedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PetMedicalRecordRepository extends JpaRepository<PetMedicalRecord, Long> {
    
    List<PetMedicalRecord> findByPetId(Long petId);
    
    List<PetMedicalRecord> findByPetIdOrderByRecordDateDesc(Long petId);
    
    List<PetMedicalRecord> findByPetIdAndRecordType(Long petId, String recordType);
    
    @Query("SELECT pmr FROM PetMedicalRecord pmr WHERE pmr.petId = :petId AND " +
           "pmr.recordDate BETWEEN :fromDate AND :toDate ORDER BY pmr.recordDate DESC")
    List<PetMedicalRecord> findByPetIdAndRecordDateBetween(@Param("petId") Long petId,
                                                           @Param("fromDate") LocalDate fromDate,
                                                           @Param("toDate") LocalDate toDate);
    
    @Query("SELECT pmr FROM PetMedicalRecord pmr WHERE " +
           "(:petId IS NULL OR pmr.petId = :petId) AND " +
           "(:recordType IS NULL OR pmr.recordType = :recordType) AND " +
           "(:veterinarian IS NULL OR pmr.veterinarian LIKE %:veterinarian%) AND " +
           "(:clinicName IS NULL OR pmr.clinicName LIKE %:clinicName%)")
    List<PetMedicalRecord> findRecordsByConditions(@Param("petId") Long petId,
                                                   @Param("recordType") String recordType,
                                                   @Param("veterinarian") String veterinarian,
                                                   @Param("clinicName") String clinicName);
    
    long countByPetId(Long petId);
    
    @Query("SELECT DISTINCT pmr.recordType FROM PetMedicalRecord pmr ORDER BY pmr.recordType")
    List<String> findDistinctRecordTypes();
    
    @Query("SELECT DISTINCT pmr.veterinarian FROM PetMedicalRecord pmr WHERE pmr.veterinarian IS NOT NULL ORDER BY pmr.veterinarian")
    List<String> findDistinctVeterinarians();
    
    @Query("SELECT DISTINCT pmr.clinicName FROM PetMedicalRecord pmr WHERE pmr.clinicName IS NOT NULL ORDER BY pmr.clinicName")
    List<String> findDistinctClinicNames();
}