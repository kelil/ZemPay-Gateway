-- Add environment-specific provider configuration.
ALTER TABLE merchant_provider_configs
    ADD COLUMN environment VARCHAR(20) NOT NULL DEFAULT 'LIVE';

-- Restrict supported environments.
ALTER TABLE merchant_provider_configs
    ADD CONSTRAINT chk_provider_config_environment
    CHECK (environment IN ('TEST', 'LIVE'));

-- Replace the old uniqueness rule.
ALTER TABLE merchant_provider_configs
    DROP CONSTRAINT uq_merchant_provider;

ALTER TABLE merchant_provider_configs
    ADD CONSTRAINT uq_merchant_provider_environment
    UNIQUE (merchant_id, provider_id, environment);

-- Support environment-specific enabled-provider lookups.
CREATE INDEX idx_provider_configs_merchant_environment_enabled
    ON merchant_provider_configs (merchant_id, environment, enabled);