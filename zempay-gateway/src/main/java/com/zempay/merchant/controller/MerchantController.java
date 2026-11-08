package com.zempay.merchant.controller;

import com.zempay.merchant.dto.MerchantRegistrationRequest;
import com.zempay.merchant.dto.MerchantResponse;
import com.zempay.merchant.entity.Merchant;
import com.zempay.merchant.service.MerchantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping
    public ResponseEntity<MerchantResponse> registerMerchant(
            @Valid @RequestBody MerchantRegistrationRequest request) {

        System.out.println(">>> MERCHANT CONTROLLER REACHED <<<");
        Merchant merchant = merchantService.registerMerchant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(MerchantResponse.from(merchant));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MerchantResponse> getMerchantById(
            @PathVariable UUID id) {

        Merchant merchant = merchantService.getMerchantById(id);

        return ResponseEntity.ok(MerchantResponse.from(merchant));
    }

    @GetMapping
    public ResponseEntity<Page<MerchantResponse>> getMerchants(
            @PageableDefault(size = 10, sort = "createdAt")
            Pageable pageable) {

        Page<MerchantResponse> merchants = merchantService
                .getMerchants(pageable)
                .map(MerchantResponse::from);

        return ResponseEntity.ok(merchants);
    }
}