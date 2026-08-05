package hn.infatlan.msvc_transactions.services.implementations;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hn.infatlan.msvc_transactions.entities.Client;
import hn.infatlan.msvc_transactions.entities.ClientStatus;
import hn.infatlan.msvc_transactions.enums.CatalogClientStatus;
import hn.infatlan.msvc_transactions.enums.ClientCode;
import hn.infatlan.msvc_transactions.enums.ProcessLogCatalog;
import hn.infatlan.msvc_transactions.repositories.ClientRepository;
import hn.infatlan.msvc_transactions.repositories.ClientStatusRepository;
import hn.infatlan.msvc_transactions.services.definitions.ClientService;
import hn.infatlan.msvc_transactions.util.ExceptionFactory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientStatusRepository clientStatusRepository;

    @Override
    @Transactional
    public Client findOrCreate(String identityNumber, String fullName, String email) {
        Optional<Client> clientFound = this.clientRepository.findByIdentityNumber(identityNumber);

        if (!clientFound.isPresent()) {
            return this.createClient(identityNumber, fullName, email);
        }

        return clientFound.get();
    }

    private Client createClient(String identityNumber, String fullName, String email) {
        validateEmailAvailable(email);

        ClientStatus activeStatus = this.clientStatusRepository.findByCode(CatalogClientStatus.ACTIVE.name())
                .orElseThrow(() -> ExceptionFactory.business(
                        ClientCode.CLIENT_NOT_FOUND,
                        ProcessLogCatalog.FIND_OR_CREATE_CLIENT,
                        "No se encontró el estado " + CatalogClientStatus.ACTIVE.name() + " del catálogo de clientes."));

        Client client = Client.builder()
                .identityNumber(identityNumber)
                .fullName(fullName)
                .email(email)
                .status(activeStatus)
                .build();

        return this.clientRepository.save(client);
    }

    private void validateEmailAvailable(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        if (clientRepository.existsByEmailIgnoreCase(email.trim())) {
            throw ExceptionFactory.business(
                    ClientCode.DUPLICATE_EMAIL,
                    ProcessLogCatalog.FIND_OR_CREATE_CLIENT,
                    "El correo electrónico ya pertenece a otro cliente.");
        }
    }
}
