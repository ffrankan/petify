package com.petify.user.repository;

import com.petify.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
    @Query("SELECT ur.roleName FROM UserRole ur WHERE ur.userId = :userId")
    List<String> findRoleNamesByUserId(@Param("userId") Long userId);
    
    List<UserRole> findByUserId(Long userId);
    
    void deleteByUserId(Long userId);
}