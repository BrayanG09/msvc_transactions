SET NOCOUNT ON;
GO

CREATE OR ALTER PROCEDURE dbo.sp_estado_cuenta
    @account_id UNIQUEIDENTIFIER,
    @from_date  DATE,
    @to_date    DATE,
    @page       INT = 0,
    @size       INT = 20
AS
BEGIN
    SET NOCOUNT ON;

    IF NOT EXISTS (SELECT 1 FROM dbo.accounts a WHERE a.id = @account_id)
    BEGIN
        RAISERROR(N'La cuenta solicitada no existe.', 16, 1);
        RETURN;
    END;

    DECLARE @from_dt DATETIME2 = CAST(@from_date AS DATETIME2);
    DECLARE @to_dt   DATETIME2 = DATEADD(MILLISECOND, -3, DATEADD(DAY, 1, CAST(@to_date AS DATETIME2)));

    DECLARE @account_number NVARCHAR(12);
    SELECT @account_number = a.account_number
    FROM dbo.accounts a
    WHERE a.id = @account_id;

    DECLARE @confirmed_status_id UNIQUEIDENTIFIER;
    SELECT @confirmed_status_id = ms.id
    FROM dbo.movement_status ms
    WHERE ms.code = N'CONFIRMED';

    IF @confirmed_status_id IS NULL
    BEGIN
        RAISERROR(N'No se encontro el estado CONFIRMED en el catalogo de movimientos.', 16, 1);
        RETURN;
    END;

    -- SALDO DE APERTURA: ULTIMO BALANCE_AFTER CONFIRMED ANTERIOR AL RANGO.
    DECLARE @opening_balance DECIMAL(19,4) = 0;
    SELECT TOP (1)
        @opening_balance = m.balance_after
    FROM dbo.movements m
    WHERE m.account_id = @account_id
      AND m.status_id = @confirmed_status_id
      AND m.occurred_at < @from_dt
    ORDER BY m.occurred_at DESC, m.created_at DESC;

    IF @opening_balance IS NULL
        SET @opening_balance = 0;

    -- SALDO DE CIERRE: ULTIMO BALANCE_AFTER CONFIRMED DENTRO DEL RANGO; SI NO HAY, OPENING.
    DECLARE @closing_balance DECIMAL(19,4) = @opening_balance;
    SELECT TOP (1)
        @closing_balance = m.balance_after
    FROM dbo.movements m
    WHERE m.account_id = @account_id
      AND m.status_id = @confirmed_status_id
      AND m.occurred_at >= @from_dt
      AND m.occurred_at <= @to_dt
    ORDER BY m.occurred_at DESC, m.created_at DESC;

    DECLARE @total_elements BIGINT = 0;
    SELECT @total_elements = COUNT(1)
    FROM dbo.movements m
    WHERE m.account_id = @account_id
      AND m.status_id = @confirmed_status_id
      AND m.occurred_at >= @from_dt
      AND m.occurred_at <= @to_dt;

    DECLARE @total_pages INT = 0;
    IF @total_elements = 0
        SET @total_pages = 0;
    ELSE
        SET @total_pages = CAST(CEILING(1.0 * @total_elements / @size) AS INT);

    -- RESULT SET 1: CABECERA DEL ESTADO DE CUENTA
    SELECT
        @account_id      AS account_id,
        @account_number  AS account_number,
        @from_date       AS from_date,
        @to_date         AS to_date,
        @opening_balance AS opening_balance,
        @closing_balance AS closing_balance,
        @page            AS page_number,
        @size            AS page_size,
        @total_elements  AS total_elements,
        @total_pages     AS total_pages;

    -- RESULT SET 2: MOVIMIENTOS PAGINADOS; BALANCE_AFTER = SALDO CORRIENTE (RUNNING BALANCE)
    SELECT
        m.id,
        mt.code           AS type_code,
        ms.code           AS status_code,
        m.amount,
        m.balance_after,
        m.description,
        m.occurred_at
    FROM dbo.movements m
    INNER JOIN dbo.movement_type mt ON mt.id = m.type_id
    INNER JOIN dbo.movement_status ms ON ms.id = m.status_id
    WHERE m.account_id = @account_id
      AND m.status_id = @confirmed_status_id
      AND m.occurred_at >= @from_dt
      AND m.occurred_at <= @to_dt
    ORDER BY m.occurred_at ASC, m.created_at ASC
    OFFSET (@page * @size) ROWS
    FETCH NEXT @size ROWS ONLY;
END;
GO
