-- Catalog: account status
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'account_status')
BEGIN
    CREATE TABLE account_status (
        id          UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
        code        NVARCHAR(30)     NOT NULL,
        description NVARCHAR(150)    NOT NULL,
        CONSTRAINT uq_account_status_code UNIQUE (code)
    );
END
GO

-- Catalog: client status
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'client_status')
BEGIN
    CREATE TABLE client_status (
        id          UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
        code        NVARCHAR(30)     NOT NULL,
        description NVARCHAR(150)    NOT NULL,
        CONSTRAINT uq_client_status_code UNIQUE (code)
    );
END
GO

-- Catalog: movement type
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'movement_type')
BEGIN
    CREATE TABLE movement_type (
        id          UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
        code        NVARCHAR(30)     NOT NULL,
        description NVARCHAR(150)    NOT NULL,
        CONSTRAINT uq_movement_type_code UNIQUE (code)
    );
END
GO

-- Catalog: movement status
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'movement_status')
BEGIN
    CREATE TABLE movement_status (
        id          UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
        code        NVARCHAR(30)     NOT NULL,
        description NVARCHAR(150)    NOT NULL,
        CONSTRAINT uq_movement_status_code UNIQUE (code)
    );
END
GO

-- Clients
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'clients')
BEGIN
    CREATE TABLE clients (
        id              UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
        identity_number NVARCHAR(13)     NOT NULL,
        full_name       NVARCHAR(150)    NOT NULL,
        email           NVARCHAR(150)    NULL,
        status_id       UNIQUEIDENTIFIER NOT NULL,
        created_by      NVARCHAR(100)    NOT NULL,
        created_at      DATETIME2        NOT NULL,
        updated_by      NVARCHAR(100)    NULL,
        updated_at      DATETIME2        NULL,
        CONSTRAINT uq_clients_identity_number UNIQUE (identity_number),
        CONSTRAINT fk_clients_status FOREIGN KEY (status_id) REFERENCES client_status(id)
    );
    CREATE INDEX ix_clients_status ON clients (status_id);
    CREATE UNIQUE INDEX uq_clients_email ON clients (email) WHERE email IS NOT NULL;
END
GO

-- Accounts
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'accounts')
BEGIN
    CREATE TABLE accounts (
        id             UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
        account_number NVARCHAR(12)     NOT NULL,
        client_id      UNIQUEIDENTIFIER NOT NULL,
        balance        DECIMAL(19,4)    NOT NULL CONSTRAINT ck_accounts_balance_non_negative CHECK (balance >= 0),
        currency       NVARCHAR(3)          NOT NULL CONSTRAINT df_accounts_currency DEFAULT ('HNL'),
        status_id      UNIQUEIDENTIFIER NOT NULL,
        version        BIGINT           NOT NULL CONSTRAINT df_accounts_version DEFAULT (0),
        created_by     NVARCHAR(100)    NOT NULL,
        created_at     DATETIME2        NOT NULL,
        updated_by     NVARCHAR(100)    NULL,
        updated_at     DATETIME2        NULL,
        CONSTRAINT uq_accounts_account_number UNIQUE (account_number),
        CONSTRAINT fk_accounts_client FOREIGN KEY (client_id) REFERENCES clients(id),
        CONSTRAINT fk_accounts_status FOREIGN KEY (status_id) REFERENCES account_status(id)
    );
    CREATE INDEX ix_accounts_client_status ON accounts (client_id, status_id);
END
GO

-- Movements
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'movements')
BEGIN
    CREATE TABLE movements (
        id                UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
        account_id        UNIQUEIDENTIFIER NOT NULL,
        type_id           UNIQUEIDENTIFIER NOT NULL,
        status_id         UNIQUEIDENTIFIER NOT NULL,
        amount            DECIMAL(19,4)    NOT NULL CONSTRAINT ck_movements_amount_positive CHECK (amount > 0),
        balance_after     DECIMAL(19,4)    NOT NULL,
        description       NVARCHAR(255)    NULL,
        occurred_at       DATETIME2        NOT NULL,
        correlation_id    NVARCHAR(64)     NULL,
        external_auth_ref NVARCHAR(100)    NULL,
        created_by        NVARCHAR(100)    NOT NULL,
        created_at        DATETIME2        NOT NULL,
        updated_by        NVARCHAR(100)    NULL,
        updated_at        DATETIME2        NULL,
        CONSTRAINT fk_movements_account FOREIGN KEY (account_id) REFERENCES accounts(id),
        CONSTRAINT fk_movements_type FOREIGN KEY (type_id) REFERENCES movement_type(id),
        CONSTRAINT fk_movements_status FOREIGN KEY (status_id) REFERENCES movement_status(id)
    );
    CREATE INDEX ix_movements_account_occurred ON movements (account_id, occurred_at);
    CREATE INDEX ix_movements_account_status ON movements (account_id, status_id);
END
GO

-- Idempotency records
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'idempotency_records')
BEGIN
    CREATE TABLE idempotency_records (
        id               UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
        idempotency_key  NVARCHAR(64)     NOT NULL,
        account_id       UNIQUEIDENTIFIER NOT NULL,
        request_hash     NVARCHAR(64)     NOT NULL,
        response_payload NVARCHAR(MAX)    NOT NULL,
        http_status      INT              NOT NULL,
        created_at       DATETIME2        NOT NULL,
        CONSTRAINT uq_idempotency_key_account UNIQUE (idempotency_key, account_id),
        CONSTRAINT fk_idempotency_account FOREIGN KEY (account_id) REFERENCES accounts(id)
    );
END
GO

-- Application async logs (observability)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'application_logs')
BEGIN
    CREATE TABLE application_logs (
        log_id                   UNIQUEIDENTIFIER NOT NULL PRIMARY KEY DEFAULT NEWID(),
        correlation_id           NVARCHAR(64)     NULL,
        project                  NVARCHAR(100)    NULL,
        type                     NVARCHAR(50)     NULL,
        process                  NVARCHAR(100)    NULL,
        level                    NVARCHAR(20)     NULL,
        code                     NVARCHAR(50)     NULL,
        message                  NVARCHAR(255)    NULL,
        description              NVARCHAR(MAX)    NULL,
        http_code                INT              NULL,
        user_identifier          NVARCHAR(100)    NULL,
        metadata                 NVARCHAR(MAX)    NULL,
        path                     NVARCHAR(500)    NULL,
        exception_class          NVARCHAR(255)    NULL,
        exception_message        NVARCHAR(MAX)    NULL,
        exception_cause_class    NVARCHAR(255)    NULL,
        exception_cause_message  NVARCHAR(MAX)    NULL,
        exception_stack_trace    NVARCHAR(MAX)    NULL,
        created_at               DATETIME2        NOT NULL CONSTRAINT df_application_logs_created_at DEFAULT (SYSUTCDATETIME())
    );
    CREATE INDEX ix_application_logs_correlation ON application_logs (correlation_id);
    CREATE INDEX ix_application_logs_created_at ON application_logs (created_at);
END
GO
