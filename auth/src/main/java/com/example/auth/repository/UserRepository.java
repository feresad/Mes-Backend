package com.example.auth.repository;

import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u WHERE u.username = ?1")
    Optional<User> findByUsern(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
    List<User> findByRoleName(String roleName);
}
