package hn.infatlan.msvc_transactions.controllers;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hn.infatlan.msvc_transactions.dtos.common.ResponseDTO;
import hn.infatlan.msvc_transactions.dtos.statement.StatementResponseDTO;
import hn.infatlan.msvc_transactions.enums.StatementCode;
import hn.infatlan.msvc_transactions.services.definitions.StatementService;
import hn.infatlan.msvc_transactions.util.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts/{accountId}/statement")
@RequiredArgsConstructor
@Validated
@Tag(name = "Statements", description = "Estado de cuenta vía procedimiento almacenado sp_estado_cuenta")
public class StatementController {

    private final StatementService statementService;

    @PostMapping
    @Operation(summary = "Generar estado de cuenta", description = "Servicio REST para devolver movimientos paginados con saldo corriente.")
    public ResponseEntity<ResponseDTO<StatementResponseDTO>> getStatement(
            @PathVariable("accountId") @NotNull(message = "El ID de la cuenta es obligatorio.") UUID accountId,
            @RequestParam("from") @NotNull(message = "La fecha de inicio es obligatoria.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @NotNull(message = "La fecha de fin es obligatoria.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "page", defaultValue = "0") @Min(value = 0, message = "El número de página debe ser mayor o igual a cero.") int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(value = 1, message = "El tamaño de página debe ser al menos 1.") @Max(value = 100, message = "El tamaño de página no puede exceder 100.") int size,
            @RequestHeader(RequestHeaders.IDENTIFIER_USER) @NotBlank(message = "El encabezado identifier-user es obligatorio.") String identifierUser) {

        StatementResponseDTO data = statementService.getStatement(accountId, from, to, page, size);
        return ResponseEntity.ok(ResponseDTO.of(StatementCode.STATEMENT_GENERATED, data));
    }
}
