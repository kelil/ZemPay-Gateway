package com.zempay.admin.security;

import com.zempay.admin.service.AdministratorService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AdministratorAuthenticationListener {

    private final AdministratorService administratorService;

    public AdministratorAuthenticationListener(
            AdministratorService administratorService) {
        this.administratorService = administratorService;
    }

    @EventListener
    public void handleAuthenticationSuccess(
            AuthenticationSuccessEvent event) {

        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            administratorService.recordSuccessfulAuthentication(
                    userDetails.getUsername()
            );
        }
    }
}