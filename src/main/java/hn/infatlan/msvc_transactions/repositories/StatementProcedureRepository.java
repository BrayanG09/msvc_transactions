package hn.infatlan.msvc_transactions.repositories;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import hn.infatlan.msvc_transactions.dtos.statement.StatementMovementItemDTO;
import hn.infatlan.msvc_transactions.dtos.statement.StatementResponseDTO;
import hn.infatlan.msvc_transactions.enums.AccountCode;
import hn.infatlan.msvc_transactions.enums.ProcessLogCatalog;
import hn.infatlan.msvc_transactions.enums.StatementCode;
import hn.infatlan.msvc_transactions.enums.TypeLogCatalog;
import hn.infatlan.msvc_transactions.util.ExceptionFactory;
import hn.infatlan.msvc_transactions.util.MaskingUtils;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StatementProcedureRepository {

    private static final String SP_CALL = "{call dbo.sp_estado_cuenta(?, ?, ?, ?, ?)}";

    private final JdbcTemplate jdbcTemplate;

    public StatementResponseDTO executeEstadoCuenta(
            UUID accountId,
            LocalDate from,
            LocalDate to,
            int page,
            int size) {
        try {
            return jdbcTemplate.execute((ConnectionCallback<StatementResponseDTO>) connection ->
                    invokeProcedure(connection, accountId, from, to, page, size));
        } catch (DataAccessException ex) {
            throw mapSqlException(ex);
        }
    }

    private StatementResponseDTO invokeProcedure(
            Connection connection,
            UUID accountId,
            LocalDate from,
            LocalDate to,
            int page,
            int size) throws SQLException {

        try (CallableStatement cs = connection.prepareCall(SP_CALL)) {
            cs.setObject(1, accountId);
            cs.setDate(2, Date.valueOf(from));
            cs.setDate(3, Date.valueOf(to));
            cs.setInt(4, page);
            cs.setInt(5, size);

            boolean hasResult = cs.execute();

            StatementResponseDTO.StatementResponseDTOBuilder builder = StatementResponseDTO.builder();
            List<StatementMovementItemDTO> movements = new ArrayList<>();

            int resultIndex = 0;
            while (true) {
                if (hasResult) {
                    try (ResultSet rs = cs.getResultSet()) {
                        if (resultIndex == 0) {
                            mapHeader(rs, builder);
                        } else if (resultIndex == 1) {
                            mapMovements(rs, movements);
                        }
                    }
                    resultIndex++;
                }

                if (cs.getMoreResults()) {
                    hasResult = true;
                } else if (cs.getUpdateCount() == -1) {
                    break;
                } else {
                    hasResult = false;
                }
            }

            return builder.movements(movements).build();
        }
    }

    private void mapHeader(ResultSet rs, StatementResponseDTO.StatementResponseDTOBuilder builder)
            throws SQLException {
        if (!rs.next()) {
            throw ExceptionFactory.business(
                    AccountCode.ACCOUNT_NOT_FOUND,
                    ProcessLogCatalog.GET_STATEMENT,
                    TypeLogCatalog.STATEMENT,
                    null);
        }

        builder.accountId(toUuid(rs.getObject("account_id")))
                .accountNumber(MaskingUtils.maskAccountNumber(rs.getString("account_number")))
                .from(rs.getDate("from_date").toLocalDate())
                .to(rs.getDate("to_date").toLocalDate())
                .openingBalance(rs.getBigDecimal("opening_balance"))
                .closingBalance(rs.getBigDecimal("closing_balance"))
                .page(rs.getInt("page_number"))
                .size(rs.getInt("page_size"))
                .totalElements(rs.getLong("total_elements"))
                .totalPages(rs.getInt("total_pages"));
    }

    private void mapMovements(ResultSet rs, List<StatementMovementItemDTO> movements) throws SQLException {
        while (rs.next()) {
            movements.add(StatementMovementItemDTO.builder()
                    .id(toUuid(rs.getObject("id")))
                    .type(rs.getString("type_code"))
                    .status(rs.getString("status_code"))
                    .amount(rs.getBigDecimal("amount"))
                    .balanceAfter(rs.getBigDecimal("balance_after"))
                    .description(rs.getString("description"))
                    .occurredAt(rs.getTimestamp("occurred_at").toLocalDateTime())
                    .build());
        }
    }

    private UUID toUuid(Object value) {
        if (value instanceof UUID uuid) {
            return uuid;
        }
        return UUID.fromString(value.toString());
    }

    private RuntimeException mapSqlException(DataAccessException ex) {
        String message = rootMessage(ex);
        String lower = message == null ? "" : message.toLowerCase();

        if (lower.contains("no existe") || lower.contains("cuenta solicitada")) {
            return ExceptionFactory.business(
                    AccountCode.ACCOUNT_NOT_FOUND,
                    ProcessLogCatalog.GET_STATEMENT,
                    TypeLogCatalog.STATEMENT,
                    null);
        }

        return ExceptionFactory.business(
                StatementCode.STATEMENT_GENERATED,
                ProcessLogCatalog.GET_STATEMENT,
                TypeLogCatalog.TECHNICAL,
                "Error al ejecutar el procedimiento sp_estado_cuenta.");
    }

    private String rootMessage(Throwable ex) {
        Throwable current = ex;
        String message = ex.getMessage();
        while (current.getCause() != null) {
            current = current.getCause();
            if (current.getMessage() != null) {
                message = current.getMessage();
            }
        }
        return message;
    }
}
