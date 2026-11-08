-- ============================================================
-- ZemPay Gateway
-- V1: Initial database schema
-- PostgreSQL 14+
-- ============================================================

--CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. CURRENCIES
-- ============================================================

CREATE TABLE currencies (
    code CHAR(3) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    minor_units SMALLINT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_currency_minor_units
        CHECK (minor_units BETWEEN 0 AND 6)
);

INSERT INTO currencies (code, name, minor_units, enabled)
VALUES
    ('ETB', 'Ethiopian Birr', 2, TRUE),
    ('USD', 'US Dollar', 2, FALSE),
    ('EUR', 'Euro', 2, FALSE),
    ('KES', 'Kenyan Shilling', 2, FALSE);

-- ============================================================
-- 2. MERCHANTS
-- ============================================================

CREATE TABLE merchants (
    id UUID PRIMARY KEY,
    merchant_code VARCHAR(50) NOT NULL UNIQUE,
    business_name VARCHAR(200) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    approved_by UUID,
    approved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_merchant_status
        CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'REJECTED')),

    CONSTRAINT chk_merchant_approval
        CHECK (
            (status = 'ACTIVE' AND approved_by IS NOT NULL
                               AND approved_at IS NOT NULL)
            OR
            (status <> 'ACTIVE')
        )
);

CREATE INDEX idx_merchants_status ON merchants(status);
CREATE INDEX idx_merchants_email ON merchants(email);

-- ============================================================
-- 3. MERCHANT API KEYS
-- ============================================================

CREATE TABLE merchant_api_keys (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    key_prefix VARCHAR(20) NOT NULL UNIQUE,
    key_hash VARCHAR(255) NOT NULL,
    environment VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMPTZ,
    last_used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_api_keys_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id),

    CONSTRAINT chk_api_key_environment
        CHECK (environment IN ('TEST', 'LIVE')),

    CONSTRAINT chk_api_key_status
        CHECK (status IN ('ACTIVE', 'REVOKED', 'EXPIRED'))
);

CREATE INDEX idx_api_keys_merchant
    ON merchant_api_keys(merchant_id);

-- ============================================================
-- 4. PAYMENT PROVIDERS
-- ============================================================

CREATE TABLE payment_providers (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    provider_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    supports_refunds BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_provider_type
        CHECK (provider_type IN (
            'BANK',
            'MOBILE_MONEY',
            'CARD',
            'OTHER'
        )),

    CONSTRAINT chk_provider_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE'))
);

-- Initial provider registry.
-- These are registrations only; no live integration is implied.

INSERT INTO payment_providers
    (id, code, name, provider_type, status)
VALUES
    (
        '10000000-0000-0000-0000-000000000001',
        'COREBANK360',
        'CoreBank360',
        'BANK',
        'INACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000002',
        'TELEBIRR',
        'Telebirr',
        'MOBILE_MONEY',
        'INACTIVE'
    ),
    (
        '10000000-0000-0000-0000-000000000003',
        'CBE_BIRR',
        'CBE Birr',
        'MOBILE_MONEY',
        'INACTIVE'
    );

-- ============================================================
-- 5. MERCHANT PROVIDER CONFIGURATIONS
-- ============================================================

CREATE TABLE merchant_provider_configs (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    provider_id UUID NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    priority INTEGER NOT NULL DEFAULT 100,
    credential_data JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_provider_config_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id),

    CONSTRAINT fk_provider_config_provider
        FOREIGN KEY (provider_id)
        REFERENCES payment_providers(id),

    CONSTRAINT uq_merchant_provider
        UNIQUE (merchant_id, provider_id),

    CONSTRAINT chk_provider_priority
        CHECK (priority > 0)
);

CREATE INDEX idx_provider_configs_merchant_enabled
    ON merchant_provider_configs(merchant_id, enabled);

-- ============================================================
-- 6. PAYMENTS
-- ============================================================

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    merchant_reference VARCHAR(100) NOT NULL,
    amount NUMERIC(20, 8) NOT NULL,
    currency_code CHAR(3) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CREATED',
    description VARCHAR(500),
    selected_provider_id UUID,
    return_url TEXT,
    expires_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id),

    CONSTRAINT fk_payment_currency
        FOREIGN KEY (currency_code)
        REFERENCES currencies(code),

    CONSTRAINT fk_payment_selected_provider
        FOREIGN KEY (selected_provider_id)
        REFERENCES payment_providers(id),

    CONSTRAINT uq_payment_merchant_reference
        UNIQUE (merchant_id, merchant_reference),

    CONSTRAINT chk_payment_amount
        CHECK (amount > 0),

    CONSTRAINT chk_payment_status
        CHECK (status IN (
            'CREATED',
            'PROCESSING',
            'PENDING',
            'REQUIRES_ACTION',
            'SUCCEEDED',
            'FAILED',
            'CANCELLED',
            'EXPIRED'
        ))
);

CREATE INDEX idx_payments_merchant_created
    ON payments(merchant_id, created_at DESC);

CREATE INDEX idx_payments_status_created
    ON payments(status, created_at);

CREATE INDEX idx_payments_selected_provider
    ON payments(selected_provider_id);

-- ============================================================
-- 7. PAYMENT ATTEMPTS
-- ============================================================

CREATE TABLE payment_attempts (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL,
    provider_id UUID NOT NULL,
    attempt_number INTEGER NOT NULL,
    provider_reference VARCHAR(150),
    status VARCHAR(30) NOT NULL DEFAULT 'INITIATED',
    failure_reason TEXT,
    initiated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attempt_payment
        FOREIGN KEY (payment_id)
        REFERENCES payments(id),

    CONSTRAINT fk_attempt_provider
        FOREIGN KEY (provider_id)
        REFERENCES payment_providers(id),

    CONSTRAINT uq_payment_attempt_number
        UNIQUE (payment_id, attempt_number),

    CONSTRAINT chk_attempt_number
        CHECK (attempt_number > 0),

    CONSTRAINT chk_attempt_status
        CHECK (status IN (
            'INITIATED',
            'PENDING',
            'SUCCEEDED',
            'FAILED',
            'UNKNOWN',
            'CANCELLED'
        ))
);

CREATE UNIQUE INDEX uq_provider_reference
    ON payment_attempts(provider_id, provider_reference)
    WHERE provider_reference IS NOT NULL;

CREATE INDEX idx_attempts_payment
    ON payment_attempts(payment_id);

CREATE INDEX idx_attempts_provider_status
    ON payment_attempts(provider_id, status);

-- ============================================================
-- 8. IDEMPOTENCY KEYS
-- ============================================================

CREATE TABLE idempotency_keys (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    idempotency_key VARCHAR(150) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    payment_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ,

    CONSTRAINT fk_idempotency_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id),

    CONSTRAINT fk_idempotency_payment
        FOREIGN KEY (payment_id)
        REFERENCES payments(id),

    CONSTRAINT uq_merchant_idempotency_key
        UNIQUE (merchant_id, idempotency_key)
);

-- ============================================================
-- 9. PAYMENT STATUS HISTORY
-- ============================================================

CREATE TABLE payment_status_history (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    reason TEXT,
    changed_by VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_status_history_payment
        FOREIGN KEY (payment_id)
        REFERENCES payments(id),

    CONSTRAINT chk_history_new_status
        CHECK (new_status IN (
            'CREATED',
            'PROCESSING',
            'PENDING',
            'REQUIRES_ACTION',
            'SUCCEEDED',
            'FAILED',
            'CANCELLED',
            'EXPIRED'
        ))
);

CREATE INDEX idx_payment_history_payment
    ON payment_status_history(payment_id, created_at);

-- ============================================================
-- 10. WEBHOOK ENDPOINTS
-- ============================================================

CREATE TABLE webhook_endpoints (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    url TEXT NOT NULL,
    encrypted_secret TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_webhook_endpoint_merchant
        FOREIGN KEY (merchant_id)
        REFERENCES merchants(id)
);

CREATE INDEX idx_webhook_endpoints_merchant
    ON webhook_endpoints(merchant_id);

-- ============================================================
-- 11. WEBHOOK EVENTS
-- ============================================================

CREATE TABLE webhook_events (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL,
    endpoint_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB NOT NULL,
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_webhook_event_payment
        FOREIGN KEY (payment_id)
        REFERENCES payments(id),

    CONSTRAINT fk_webhook_event_endpoint
        FOREIGN KEY (endpoint_id)
        REFERENCES webhook_endpoints(id),

    CONSTRAINT chk_webhook_delivery_status
        CHECK (delivery_status IN (
            'PENDING',
            'PROCESSING',
            'DELIVERED',
            'FAILED'
        )),

    CONSTRAINT chk_webhook_retry_count
        CHECK (retry_count >= 0),

    CONSTRAINT uq_webhook_payment_endpoint_event
        UNIQUE (payment_id, endpoint_id, event_type)
);

CREATE INDEX idx_webhook_events_pending
    ON webhook_events(delivery_status, next_retry_at);

-- ============================================================
-- 12. AUDIT LOGS
-- ============================================================

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_type VARCHAR(30) NOT NULL,
    actor_id UUID,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_entity
    ON audit_logs(entity_type, entity_id);

CREATE INDEX idx_audit_logs_created
    ON audit_logs(created_at DESC);