package com.prorenta.financeservice.service.impl.integration_tests.TransactionServiceImplIntegrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prorenta.financeservice.model.dto.UpdateTransactionRequestDto;
import com.prorenta.financeservice.service.impl.integration_tests.AbstractIntegrationTest;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

public class RemoveTransactionServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Sql(
            scripts = {
                    "/sql/cleanup.sql",
                    "/sql/insert_filter_data.sql"
            },
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @SneakyThrows
    @DisplayName("Удаление транзакции: мягкое удаление транзакции и проверка её скрытия из БД")
    public void softDeleteTransactionIntegrationTest() {
        UUID transactionId = UUID.fromString("44444444-4444-4444-4444-444444444441");

        MvcResult deleteResult = mockMvc.perform(delete("/api/v1/transactions/{id}", transactionId))
                .andReturn();

        Assertions.assertThat(deleteResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NO_CONTENT.value());

        UpdateTransactionRequestDto updateRequest = UpdateTransactionRequestDto.builder().build();

        MvcResult patchResult = mockMvc.perform(patch("/api/v1/transactions/{id}", transactionId)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andReturn();

        Assertions.assertThat(patchResult.getResponse().getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());
    }
}
