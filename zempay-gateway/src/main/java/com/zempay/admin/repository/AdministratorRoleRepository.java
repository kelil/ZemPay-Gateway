package com.zempay.admin.repository;

import com.zempay.admin.entity.AdministratorRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministratorRoleRepository
        extends JpaRepository<AdministratorRole, Long> {

    Optional<AdministratorRole> findByRoleCode(String roleCode);
}