-- Seed catalogs
IF NOT EXISTS (SELECT 1 FROM account_status WHERE code = 'ACTIVE')
    INSERT INTO account_status (id, code, description) VALUES (NEWID(), 'ACTIVE', 'Cuenta activa');
IF NOT EXISTS (SELECT 1 FROM account_status WHERE code = 'BLOCKED')
    INSERT INTO account_status (id, code, description) VALUES (NEWID(), 'BLOCKED', 'Cuenta bloqueada');
IF NOT EXISTS (SELECT 1 FROM account_status WHERE code = 'CLOSED')
    INSERT INTO account_status (id, code, description) VALUES (NEWID(), 'CLOSED', 'Cuenta cerrada');

IF NOT EXISTS (SELECT 1 FROM client_status WHERE code = 'ACTIVE')
    INSERT INTO client_status (id, code, description) VALUES (NEWID(), 'ACTIVE', 'Cliente activo');
IF NOT EXISTS (SELECT 1 FROM client_status WHERE code = 'INACTIVE')
    INSERT INTO client_status (id, code, description) VALUES (NEWID(), 'INACTIVE', 'Cliente inactivo');

IF NOT EXISTS (SELECT 1 FROM movement_type WHERE code = 'CREDIT')
    INSERT INTO movement_type (id, code, description) VALUES (NEWID(), 'CREDIT', 'Crédito');
IF NOT EXISTS (SELECT 1 FROM movement_type WHERE code = 'DEBIT')
    INSERT INTO movement_type (id, code, description) VALUES (NEWID(), 'DEBIT', 'Débito');
IF NOT EXISTS (SELECT 1 FROM movement_type WHERE code = 'OPENING')
    INSERT INTO movement_type (id, code, description) VALUES (NEWID(), 'OPENING', 'Apertura de cuenta');

IF NOT EXISTS (SELECT 1 FROM movement_status WHERE code = 'PENDING')
    INSERT INTO movement_status (id, code, description) VALUES (NEWID(), 'PENDING', 'Pendiente');
IF NOT EXISTS (SELECT 1 FROM movement_status WHERE code = 'CONFIRMED')
    INSERT INTO movement_status (id, code, description) VALUES (NEWID(), 'CONFIRMED', 'Confirmado');
IF NOT EXISTS (SELECT 1 FROM movement_status WHERE code = 'REJECTED')
    INSERT INTO movement_status (id, code, description) VALUES (NEWID(), 'REJECTED', 'Rechazado');
