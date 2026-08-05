package hn.infatlan.msvc_transactions.support;

import hn.infatlan.msvc_transactions.entities.AccountStatus;
import hn.infatlan.msvc_transactions.entities.ClientStatus;
import hn.infatlan.msvc_transactions.entities.MovementStatus;
import hn.infatlan.msvc_transactions.entities.MovementType;
import hn.infatlan.msvc_transactions.enums.CatalogAccountStatus;
import hn.infatlan.msvc_transactions.enums.CatalogClientStatus;
import hn.infatlan.msvc_transactions.enums.CatalogMovementStatus;
import hn.infatlan.msvc_transactions.enums.CatalogMovementType;
import hn.infatlan.msvc_transactions.repositories.AccountStatusRepository;
import hn.infatlan.msvc_transactions.repositories.ClientStatusRepository;
import hn.infatlan.msvc_transactions.repositories.MovementStatusRepository;
import hn.infatlan.msvc_transactions.repositories.MovementTypeRepository;

public final class CatalogFixtures {

    private CatalogFixtures() {
    }

    public static void seedAll(
            AccountStatusRepository accountStatusRepository,
            ClientStatusRepository clientStatusRepository,
            MovementTypeRepository movementTypeRepository,
            MovementStatusRepository movementStatusRepository) {
        seedAccountStatuses(accountStatusRepository);
        seedClientStatuses(clientStatusRepository);
        seedMovementTypes(movementTypeRepository);
        seedMovementStatuses(movementStatusRepository);
    }

    public static void seedAccountStatuses(AccountStatusRepository repository) {
        saveAccountStatus(repository, CatalogAccountStatus.ACTIVE, "Cuenta activa");
        saveAccountStatus(repository, CatalogAccountStatus.BLOCKED, "Cuenta bloqueada");
        saveAccountStatus(repository, CatalogAccountStatus.CLOSED, "Cuenta cerrada");
    }

    public static void seedClientStatuses(ClientStatusRepository repository) {
        saveClientStatus(repository, CatalogClientStatus.ACTIVE, "Cliente activo");
        saveClientStatus(repository, CatalogClientStatus.INACTIVE, "Cliente inactivo");
    }

    public static void seedMovementTypes(MovementTypeRepository repository) {
        saveMovementType(repository, CatalogMovementType.CREDIT, "Credito");
        saveMovementType(repository, CatalogMovementType.DEBIT, "Debito");
        saveMovementType(repository, CatalogMovementType.OPENING, "Apertura");
    }

    public static void seedMovementStatuses(MovementStatusRepository repository) {
        saveMovementStatus(repository, CatalogMovementStatus.PENDING, "Pendiente");
        saveMovementStatus(repository, CatalogMovementStatus.CONFIRMED, "Confirmado");
        saveMovementStatus(repository, CatalogMovementStatus.REJECTED, "Rechazado");
    }

    public static AccountStatus accountStatus(CatalogAccountStatus status) {
        return AccountStatus.builder()
                .code(status.name())
                .description(status.name())
                .build();
    }

    private static void saveAccountStatus(AccountStatusRepository repository, CatalogAccountStatus status, String description) {
        if (repository.findByCode(status.name()).isEmpty()) {
            repository.save(AccountStatus.builder().code(status.name()).description(description).build());
        }
    }

    private static void saveClientStatus(ClientStatusRepository repository, CatalogClientStatus status, String description) {
        if (repository.findByCode(status.name()).isEmpty()) {
            repository.save(ClientStatus.builder().code(status.name()).description(description).build());
        }
    }

    private static void saveMovementType(MovementTypeRepository repository, CatalogMovementType type, String description) {
        if (repository.findByCode(type.name()).isEmpty()) {
            repository.save(MovementType.builder().code(type.name()).description(description).build());
        }
    }

    private static void saveMovementStatus(MovementStatusRepository repository, CatalogMovementStatus status, String description) {
        if (repository.findByCode(status.name()).isEmpty()) {
            repository.save(MovementStatus.builder().code(status.name()).description(description).build());
        }
    }
}
