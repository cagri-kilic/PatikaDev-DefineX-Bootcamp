package com.patikadev.definex.advancedtaskmanager.repository;

import com.patikadev.definex.advancedtaskmanager.model.entity.RefreshToken;
import com.patikadev.definex.advancedtaskmanager.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.isActive = false WHERE r.user.id = :userId")
    void deactivateAllUserTokens(@Param("userId") UUID userId);

    @Modifying
    void deleteByUser(User user);
} 