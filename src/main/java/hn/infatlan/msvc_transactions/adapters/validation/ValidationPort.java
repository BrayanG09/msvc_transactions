package hn.infatlan.msvc_transactions.adapters.validation;

public interface ValidationPort {
    ValidationResult authorize(ValidationRequest request);
}
