package hn.infatlan.msvc_transactions.dtos.common;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import hn.infatlan.msvc_transactions.enums.ResponseCodeCatalog;
import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDTO<T> {

  private String code;

  private String message;

  private String description;

  private LocalDateTime timestamp;

  private T data;

  public static <T> ResponseDTO<T> success(T data) {
    CodeCatalog codeCatalog = ResponseCodeCatalog.SUCCESS;

    return ResponseDTO.<T>builder()
        .code(codeCatalog.code())
        .message(codeCatalog.message())
        .description(codeCatalog.description())
        .timestamp(LocalDateTime.now())
        .data(data)
        .build();
  }

  public static <T> ResponseDTO<T> error(CodeCatalog codeCatalog, T data) {
    return ResponseDTO.<T>builder()
        .code(codeCatalog.code())
        .message(codeCatalog.message())
        .description(codeCatalog.description())
        .timestamp(LocalDateTime.now())
        .data(data)
        .build();
  }

  public ResponseDTO<T> withCustomMessage(String customMessage) {
    this.setMessage(customMessage);
    return this;
  }
}
