#!/bin/bash
set -euo pipefail

SQLCMD="/opt/mssql-tools18/bin/sqlcmd"
SERVER="${MSSQL_HOST:-sqlserver}"
SA_PASSWORD="${MSSQL_SA_PASSWORD:?MSSQL_SA_PASSWORD is required}"
DB_NAME="${MSSQL_DATABASE:-msvc_transactions}"
APP_USER="${MSSQL_APP_USER:?MSSQL_APP_USER is required}"
APP_PASSWORD="${MSSQL_APP_PASSWORD:?MSSQL_APP_PASSWORD is required}"

echo "==> Creating database [${DB_NAME}] if it does not exist..."
"${SQLCMD}" -S "${SERVER}" -U sa -P "${SA_PASSWORD}" -C -Q \
  "IF DB_ID(N'${DB_NAME}') IS NULL CREATE DATABASE [${DB_NAME}];"

echo "==> Creating application login [${APP_USER}] if it does not exist..."
"${SQLCMD}" -S "${SERVER}" -U sa -P "${SA_PASSWORD}" -C -Q "
IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = N'${APP_USER}')
BEGIN
    CREATE LOGIN [${APP_USER}] WITH PASSWORD = N'${APP_PASSWORD}', CHECK_POLICY = ON;
END
"

echo "==> Creating database user and granting permissions..."
"${SQLCMD}" -S "${SERVER}" -U sa -P "${SA_PASSWORD}" -C -d "${DB_NAME}" -Q "
IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = N'${APP_USER}')
BEGIN
    CREATE USER [${APP_USER}] FOR LOGIN [${APP_USER}];
    ALTER ROLE db_owner ADD MEMBER [${APP_USER}];
END
"

echo "==> Applying schema.sql..."
"${SQLCMD}" -S "${SERVER}" -U sa -P "${SA_PASSWORD}" -C -d "${DB_NAME}" -i /scripts/schema.sql

echo "==> Applying data.sql (catalog seeds)..."
"${SQLCMD}" -S "${SERVER}" -U sa -P "${SA_PASSWORD}" -C -d "${DB_NAME}" -i /scripts/data.sql

echo "==> Database initialization completed successfully."
