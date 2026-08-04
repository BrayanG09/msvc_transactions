package hn.infatlan.msvc_transactions.dtos.account;

import java.math.BigDecimal;

import hn.infatlan.msvc_transactions.annotations.SanitizeIdentity;
import hn.infatlan.msvc_transactions.annotations.SanitizeText;
import hn.infatlan.msvc_transactions.util.RegexConstants;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
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
public class CreateAccountRequestDTO {

    @SanitizeIdentity
    @NotBlank(message = "El número de identidad es obligatorio.")
    @Pattern(regexp = RegexConstants.IDENTITY_NUMBER_PATTERN, message = "El número de identidad debe contener exactamente 13 dígitos.")
    private String identityNumber;

    @SanitizeText(maxLength = 150)
    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(min = 2, max = 150, message = "El nombre completo debe tener entre 2 y 150 caracteres.")
    private String fullName;

    @SanitizeText(maxLength = 150, blankToNull = true)
    @Email(message = "El correo electrónico no tiene un formato válido.")
    @Size(max = 150, message = "El correo electrónico no puede exceder 150 caracteres.")
    private String email;

    @NotNull(message = "El saldo inicial es obligatorio.")
    @DecimalMin(value = "0.0", inclusive = true, message = "El saldo inicial debe ser mayor o igual a cero.")
    @Digits(integer = 15, fraction = 4, message = "El saldo inicial excede la precisión permitida.")
    private BigDecimal initialBalance;
}
