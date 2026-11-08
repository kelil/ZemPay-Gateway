package com.zempay.config;

import com.zempay.admin.security.AdministratorUserDetailsService;
import com.zempay.authentication.security.MerchantApiKeyAuthenticationFilter;
import com.zempay.authentication.security.MerchantApiKeyAuthenticationProvider;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public DaoAuthenticationProvider administratorAuthenticationProvider(
            AdministratorUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider administratorAuthenticationProvider,
            MerchantApiKeyAuthenticationProvider merchantAuthenticationProvider
    ) {
        return new ProviderManager(
                administratorAuthenticationProvider,
                merchantAuthenticationProvider
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager)
            throws Exception {

        MerchantApiKeyAuthenticationFilter merchantFilter =
                new MerchantApiKeyAuthenticationFilter(authenticationManager);
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationManager(authenticationManager)
                .addFilterBefore(merchantFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth

                        // Allow Spring Boot error dispatches
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // Public merchant registration
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/merchants"
                        ).permitAll()

                        // Public health endpoint
                        .requestMatchers("/actuator/health").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}