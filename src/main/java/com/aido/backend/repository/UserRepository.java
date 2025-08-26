package com.aido.backend.repository;

import com.aido.backend.entity.User;
import com.aido.backend.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> findByKeyword(@Param("keyword") String keyword);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
    
    List<User> findByProvider(AuthProvider provider);
    
    boolean existsByProviderAndProviderId(AuthProvider provider, String providerId);
}