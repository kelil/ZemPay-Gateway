package com.zempay.payment.repository;

import com.zempay.payment.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, String> {

    Optional<Currency> findByCodeAndEnabledTrue(String code);

    List<Currency> findAllByEnabledTrue();
}