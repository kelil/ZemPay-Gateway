package com.zempay.merchant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MerchantRegistrationRequest(

        @NotBlank
        @Size(max = 200)
        String businessName,

        @NotBlank
        @Size(max = 150)
        String displayName,

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 30)
        String phone

) {

}