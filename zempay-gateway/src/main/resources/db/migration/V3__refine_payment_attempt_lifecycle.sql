-- Replace the existing attempt status constraint.
ALTER TABLE payment_attempts
    DROP CONSTRAINT chk_attempt_status;

-- Add the agreed payment attempt lifecycle statuses.
ALTER TABLE payment_attempts
    ADD CONSTRAINT chk_attempt_status
    CHECK (
        status IN (
            'CREATED',
            'SUBMITTED',
            'PROCESSING',
            'PENDING',
            'SUCCEEDED',
            'FAILED',
            'UNKNOWN',
            'CANCELLED'
        )
    );

-- Use CREATED as the initial attempt status.
ALTER TABLE payment_attempts
    ALTER COLUMN status SET DEFAULT 'CREATED';

-- Allow only one unresolved attempt per payment.
CREATE UNIQUE INDEX uq_one_unresolved_attempt_per_payment
    ON payment_attempts (payment_id)
    WHERE status IN (
        'CREATED',
        'SUBMITTED',
        'PROCESSING',
        'PENDING',
        'UNKNOWN'
    );