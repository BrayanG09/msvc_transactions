package hn.infatlan.msvc_transactions.interfaces;

import org.springframework.http.HttpStatus;

public interface CodeCatalog {
    HttpStatus httpCode();
    String code();
    String message();
    String description();
}
