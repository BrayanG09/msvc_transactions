package hn.infatlan.msvc_transactions.dtos.transaction;

import java.math.BigDecimal;

import hn.infatlan.msvc_transactions.annotations.SanitizeText;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequestDTO {

    @NotBlank(message = "El tipo de movimiento es obligatorio.")
    @Pattern(regexp = "CREDIT|DEBIT", message = "El tipo de movimiento debe ser CREDIT o DEBIT.")
    private String type;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser mayor a cero.")
    @Digits(integer = 15, fraction = 4, message = "El monto excede la precisión permitida.")
    private BigDecimal amount;

    @SanitizeText(maxLength = 255, blankToNull = true)
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres.")
    private String description;
}
