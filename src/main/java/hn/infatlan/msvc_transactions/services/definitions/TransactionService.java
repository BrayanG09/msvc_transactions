package hn.infatlan.msvc_transactions.services.definitions;

import java.util.UUID;

import hn.infatlan.msvc_transactions.dtos.transaction.CreateTransactionRequestDTO;
import hn.infatlan.msvc_transactions.dtos.transaction.TransactionResponseDTO;

public interface TransactionService {

    TransactionResponseDTO createTransaction(
            UUID accountId,
            CreateTransactionRequestDTO request,
            String idempotencyKey,
            String identifierUser,
            String correlationId);
}
