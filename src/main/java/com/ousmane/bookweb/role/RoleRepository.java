package com.ousmane.bookweb.role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RoleRepository
 */
public interface RoleRepository extends JpaRepository<Role,Integer>{
    Optional<Role> findByName(String role);
    
}