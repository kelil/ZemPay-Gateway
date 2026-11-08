package com.zempay.admin.repository;

import com.zempay.admin.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdministratorRepository
        extends JpaRepository<Administrator, UUID> {

    Optional<Administrator> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}