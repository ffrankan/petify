package com.petify.user.repository;

import com.petify.user.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {
    
    List<UserAuth> findByUserId(Long userId);
    
    Optional<UserAuth> findByUserIdAndAuthType(Long userId, String authType);
    
    void deleteByUserId(Long userId);
}