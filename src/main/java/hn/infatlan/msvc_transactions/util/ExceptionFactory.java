package hn.infatlan.msvc_transactions.util;

import hn.infatlan.msvc_transactions.enums.LevelLogCatalog;
import hn.infatlan.msvc_transactions.enums.ProcessLogCatalog;
import hn.infatlan.msvc_transactions.enums.ProjectsCatalog;
import hn.infatlan.msvc_transactions.enums.TypeLogCatalog;
import hn.infatlan.msvc_transactions.exceptions.InfatlanTransactionException;
import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;

public final class ExceptionFactory {

    private ExceptionFactory() {
    }

    public static InfatlanTransactionException business(CodeCatalog codeCatalog, ProcessLogCatalog process) {
        return InfatlanTransactionException.builder()
                .project(ProjectsCatalog.MSVC_TRANSACTIONS)
                .type(TypeLogCatalog.ACCOUNT)
                .process(process)
                .level(LevelLogCatalog.WARN)
                .codeCatalog(codeCatalog)
                .build();
    }

    public static InfatlanTransactionException business(
            CodeCatalog codeCatalog,
            ProcessLogCatalog process,
            String customMessage) {
        return InfatlanTransactionException.builder()
                .project(ProjectsCatalog.MSVC_TRANSACTIONS)
                .type(TypeLogCatalog.ACCOUNT)
                .process(process)
                .level(LevelLogCatalog.WARN)
                .codeCatalog(codeCatalog)
                .withCustomMessage(customMessage)
                .build();
    }
}
