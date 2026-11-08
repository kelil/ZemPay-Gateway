package com.zempay.admin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "administrator_roles")
public class AdministratorRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, unique = true, length = 30)
    private String roleCode;

    @Column(length = 255)
    private String description;

    protected AdministratorRole() {
    }

    public Long getId() {
        return id;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getDescription() {
        return description;
    }
}