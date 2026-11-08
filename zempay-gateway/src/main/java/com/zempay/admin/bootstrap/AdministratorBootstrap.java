package com.zempay.admin.bootstrap;

import com.zempay.admin.dto.AdministratorCreateRequest;
import com.zempay.admin.repository.AdministratorRepository;
import com.zempay.admin.service.AdministratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AdministratorBootstrap implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(AdministratorBootstrap.class);

    private final AdministratorRepository administratorRepository;
    private final AdministratorService administratorService;

    @Value("${zempay.bootstrap.admin.name:}")
    private String adminName;

    @Value("${zempay.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${zempay.bootstrap.admin.password:}")
    private String adminPassword;

    public AdministratorBootstrap(
            AdministratorRepository administratorRepository,
            AdministratorService administratorService) {
        this.administratorRepository = administratorRepository;
        this.administratorService = administratorService;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (administratorRepository.count() > 0) {
            log.info("Administrator account already exists. Bootstrap skipped.");
            return;
        }

        if (adminName.isBlank()
                || adminEmail.isBlank()
                || adminPassword.isBlank()) {
            throw new IllegalStateException(
                    "No administrators exist. Configure "
                            + "ZEMPAY_BOOTSTRAP_ADMIN_NAME, "
                            + "ZEMPAY_BOOTSTRAP_ADMIN_EMAIL and "
                            + "ZEMPAY_BOOTSTRAP_ADMIN_PASSWORD."
            );
        }

        if (adminPassword.length() < 12 || adminPassword.length() > 72) {
            throw new IllegalStateException(
                    "Bootstrap administrator password must be between 12 and 72 characters."
            );
        }

        AdministratorCreateRequest request =
                new AdministratorCreateRequest(
                        adminName,
                        adminEmail,
                        adminPassword,
                        Set.of("SUPER_ADMIN")
                );

        administratorService.createAdministrator(request);

        log.info("Initial SUPER_ADMIN account created for email: {}",
                adminEmail);
    }
}