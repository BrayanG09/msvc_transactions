package hn.infatlan.msvc_transactions.services.strategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import hn.infatlan.msvc_transactions.enums.CatalogMovementType;
import hn.infatlan.msvc_transactions.enums.ProcessLogCatalog;
import hn.infatlan.msvc_transactions.enums.TransactionCode;
import hn.infatlan.msvc_transactions.enums.TypeLogCatalog;
import hn.infatlan.msvc_transactions.util.ExceptionFactory;

@Component
public class TransactionStrategyFactory {

    private final Map<CatalogMovementType, TransactionStrategy> strategies;

    public TransactionStrategyFactory(List<TransactionStrategy> strategyList) {
        this.strategies = new EnumMap<>(CatalogMovementType.class);
        for (TransactionStrategy strategy : strategyList) {
            strategies.put(strategy.supports(), strategy);
        }
    }

    public TransactionStrategy resolve(String type) {
        CatalogMovementType movementType;
        try {
            movementType = CatalogMovementType.valueOf(type);
        } catch (IllegalArgumentException ex) {
            throw ExceptionFactory.business(
                    TransactionCode.INVALID_MOVEMENT_TYPE,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TRANSACTION,
                    null);
        }

        if (movementType == CatalogMovementType.OPENING) {
            throw ExceptionFactory.business(
                    TransactionCode.INVALID_MOVEMENT_TYPE,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TRANSACTION,
                    null);
        }

        TransactionStrategy strategy = strategies.get(movementType);
        if (strategy == null) {
            throw ExceptionFactory.business(
                    TransactionCode.INVALID_MOVEMENT_TYPE,
                    ProcessLogCatalog.CREATE_TRANSACTION,
                    TypeLogCatalog.TRANSACTION,
                    null);
        }
        return strategy;
    }
}
