package com.zempay.admin.security;

import com.zempay.admin.entity.Administrator;
import com.zempay.admin.repository.AdministratorRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AdministratorUserDetailsService implements UserDetailsService {

    private final AdministratorRepository administratorRepository;

    public AdministratorUserDetailsService(
            AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        Administrator administrator = administratorRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Administrator account not found."
                ));

        var authorities = administrator.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(
                        "ROLE_" + role.getRoleCode()
                ))
                .toList();

        return User.builder()
                .username(administrator.getEmail())
                .password(administrator.getPasswordHash())
                .authorities(authorities)
                .disabled(administrator.getStatus().name().equals("DISABLED"))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }
}