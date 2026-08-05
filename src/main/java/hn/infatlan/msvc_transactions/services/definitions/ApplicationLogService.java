package hn.infatlan.msvc_transactions.services.definitions;

import hn.infatlan.msvc_transactions.exceptions.InfatlanTransactionException;
import hn.infatlan.msvc_transactions.interfaces.CodeCatalog;

public interface ApplicationLogService {

    void logBusinessException(InfatlanTransactionException exception, String path);

    void logUnhandledException(Exception exception, CodeCatalog codeCatalog, String path);
}
