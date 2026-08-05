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
        return business(codeCatalog, process, TypeLogCatalog.BUSINESS, null);
    }

    public static InfatlanTransactionException business(
            CodeCatalog codeCatalog,
            ProcessLogCatalog process,
            String customMessage) {
        return business(codeCatalog, process, TypeLogCatalog.BUSINESS, customMessage);
    }

    public static InfatlanTransactionException business(
            CodeCatalog codeCatalog,
            ProcessLogCatalog process,
            TypeLogCatalog type,
            String customMessage) {
        return InfatlanTransactionException.builder()
                .project(ProjectsCatalog.MSVC_TRANSACTIONS)
                .type(type)
                .process(process)
                .level(LevelLogCatalog.WARN)
                .codeCatalog(codeCatalog)
                .withCustomMessage(customMessage)
                .build();
    }
}
