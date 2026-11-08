
CREATE TABLE administrators (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_administrators_email UNIQUE (email),
    CONSTRAINT chk_administrators_status
        CHECK (status IN ('ACTIVE', 'DISABLED'))
);

CREATE UNIQUE INDEX uq_administrators_email_lower
    ON administrators (LOWER(email));


CREATE TABLE administrator_roles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_code VARCHAR(30) NOT NULL UNIQUE,
    description VARCHAR(255)
);


CREATE TABLE administrator_role_assignments (
    administrator_id UUID NOT NULL,
    role_id BIGINT NOT NULL,

    PRIMARY KEY (administrator_id, role_id),

    CONSTRAINT fk_admin_role_assignments_admin
        FOREIGN KEY (administrator_id)
        REFERENCES administrators(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_admin_role_assignments_role
        FOREIGN KEY (role_id)
        REFERENCES administrator_roles(id)
        ON DELETE RESTRICT
);


INSERT INTO administrator_roles (role_code, description)
VALUES
    ('ADMIN', 'Manage merchant onboarding and operational administration'),
    ('SUPER_ADMIN', 'Manage administrators and system-wide configuration');


ALTER TABLE merchants
    ADD CONSTRAINT fk_merchants_approved_by
    FOREIGN KEY (approved_by)
    REFERENCES administrators(id);
