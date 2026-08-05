# msvc-transactions

Microservicio Spring Boot 4 (Java 17) para cuentas, movimientos CREDIT/DEBIT, estado de cuenta vía SP SQL Server, validación externa con Resilience4j, logs asíncronos y métricas Micrometer/Actuator.

## Stack Tecnologico

- Spring Boot 4.0.7 / Java 17
- SQL Server 2022
- Resilience4j con circuit breaker + retry
- Micrometer + Actuator (`/actuator/health`, `/metrics`)
- SpringDoc OpenAPI (`/swagger-ui.html`)
- WireMock

## Requisitos
- Docker + Docker Compose
- Bash (Git Bash o WSL en Windows) para los scripts de `docker/`, o PowerShell/CMD con los comandos equivalentes indicados abajo

## Arranque con Docker

Los scripts `docker/up.sh` y `docker/down.sh` funcionan en **Linux**, **macOS** y **Windows** siempre que tengas Bash disponible (terminal nativa en Linux/macOS; Git Bash, WSL).

Utilice el siguiente comando para levantar todo el stack:
```bash
bash docker/up.sh
```

Detener:
```bash
bash docker/down.sh
```

### Windows sin Bash (PowerShell)
Si usted no tiene Bash instalado, use estos comandos equivalentes desde **PowerShell** en la raíz del proyecto:

Levantar:
```powershell
Copy-Item .env.example .env -Force
docker compose --env-file .env up -d --build
```

Detener:
```powershell
docker compose --env-file .env down
```

## Documentacion
Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Runbook
Headers comunes:
- `identifier-user` — obligatorio en consultas y movimientos
- `Idempotency-Key` — obligatorio en movimientos
- `X-Correlation-Id` — opcional; si no se envía, se genera y se devuelve en el body (`correlationId`) y en el header de la respuesta

### 1. Crear cuenta
A continuacion se detalle como probar los endpoints con curl, pero adjunto al repositorio encontrara la collecion de postman para poder probarlos.

```bash
curl -s -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "identityNumber": "0801199012345",
    "fullName": "Lineth Alvarez",
    "email": "lineth.alvarez@correo.com",
    "initialBalance": 100.0000
  }'
```

### 2. Consultar cuenta

```bash
curl -s http://localhost:8080/accounts/{accountId} \
  -H "identifier-user: 0803200200876"
```

### 3. Registrar movimiento (CREDIT / DEBIT)

```bash
curl -s -X POST http://localhost:8080/accounts/{accountId}/transactions \
  -H "Content-Type: application/json" \
  -H "identifier-user: 0803200200876" \
  -H "Idempotency-Key: 8799f7d4-ae4b-4484-bfaf-802253b6a02a" \
  -H "X-Correlation-Id: bdf96569-ee51-4721-ae17-45531bfb0227" \
  -d '{
    "type": "DEBIT",
    "amount": 25.0000,
    "description": "Retiro caja"
  }'
```

### 4. Estado de cuenta

```bash
curl -s -X POST "http://localhost:8080/accounts/{accountId}/statement?from=2026-01-01&to=2026-12-31&page=0&size=20" \
  -H "identifier-user: 0803200200876"
```

## Observabilidad
- Métricas Micrometer: `GET /actuator/metrics` y `GET /actuator/metrics/{name}`
- Contadores de logs async: `application.logs.persisted`, `application.logs.persist.failures`
- Logs de negocio/técnicos en tabla SQL Server `application_logs`
- En la coleccion de POSTMAN encontrara mas ejemplos.

Ejemplo:

```bash
curl -s http://localhost:8080/actuator/metrics/http.server.requests
curl -s "http://localhost:8080/actuator/metrics/application.logs.persisted"
```

## Pruebas Automatizadas
- Tener JDK 17 instalado y en el PATH

### Linux / macOS / Git Bash en Windows:
```bash
./mvnw test
```

### Windows (CMD o PowerShell):
```bash
.\mvnw.cmd test
```