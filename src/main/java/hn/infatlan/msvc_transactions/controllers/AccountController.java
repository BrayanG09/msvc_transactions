package hn.infatlan.msvc_transactions.controllers;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hn.infatlan.msvc_transactions.dtos.account.AccountResponseDTO;
import hn.infatlan.msvc_transactions.dtos.account.CreateAccountRequestDTO;
import hn.infatlan.msvc_transactions.dtos.common.ResponseDTO;
import hn.infatlan.msvc_transactions.enums.AccountCode;
import hn.infatlan.msvc_transactions.services.definitions.AccountService;
import hn.infatlan.msvc_transactions.util.RequestHeaders;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Accounts", description = "Gestión de cuentas financieras")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Crear una cuenta", description = "Crea el cliente si no existe y una nueva cuenta. El saldo inicial debe ser >= 0.")
    public ResponseEntity<ResponseDTO<AccountResponseDTO>> createAccount(
            @Valid @RequestBody CreateAccountRequestDTO request) {
        AccountResponseDTO data = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.of(AccountCode.ACCOUNT_CREATED, data));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar una cuenta", description = "Devuelve los datos y el saldo actual de la cuenta.")
    public ResponseEntity<ResponseDTO<AccountResponseDTO>> getAccount(
            @PathVariable("id") UUID id,
            @RequestHeader(RequestHeaders.IDENTIFIER_USER)
            @NotBlank(message = "El encabezado identifier-user es obligatorio.")
            String identifierUser) {
        AccountResponseDTO data = accountService.getAccountById(id);
        return ResponseEntity.ok(ResponseDTO.of(AccountCode.ACCOUNT_FOUND, data));
    }
}
