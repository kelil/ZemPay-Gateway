package com.zempay.authentication.controller;

import com.zempay.authentication.security.MerchantPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant/test")
public class MerchantAuthenticationTestController {

    /*@GetMapping("/me")
    public MerchantPrincipal getAuthenticatedMerchant(
            Authentication authentication
    ) {
        return (MerchantPrincipal) authentication.getPrincipal();
    }*/
}