package hn.infatlan.msvc_transactions.exceptions;

import hn.infatlan.msvc_transactions.enums.LevelLogCatalog;
import hn.infatlan.msvc_transactions.enums.ProjectsCatalog;
import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InfatlanTransactionException extends RuntimeException {
    private ProjectsCatalog project;
    private String type;
    private String process;
    private LevelLogCatalog level;
    private CodeCatalog codeCatalog;
    private String userIdentifier;
    private String metadata;
    private String path;
    private Exception exception;
    private String withCustomMessage;
}
