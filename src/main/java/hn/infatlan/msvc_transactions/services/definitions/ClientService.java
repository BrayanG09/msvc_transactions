package hn.infatlan.msvc_transactions.services.definitions;

import hn.infatlan.msvc_transactions.entities.Client;

public interface ClientService {

    Client findOrCreate(String identityNumber, String fullName, String email);
}
