package hn.infatlan.msvc_transactions.services.definitions;

import java.util.UUID;

import hn.infatlan.msvc_transactions.dtos.account.AccountResponseDTO;
import hn.infatlan.msvc_transactions.dtos.account.CreateAccountRequestDTO;

public interface AccountService {

    AccountResponseDTO createAccount(CreateAccountRequestDTO request);

    AccountResponseDTO getAccountById(UUID accountId);
}
