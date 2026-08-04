package hn.infatlan.msvc_transactions.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LevelLogCatalog {
    INFO("INFO", "Información"),
    WARN("WARN", "Advertencia"),
    ERROR("ERROR", "Error"),
    DEBUG("DEBUG", "Depuración");

    private final String value;
    private final String description;
}
