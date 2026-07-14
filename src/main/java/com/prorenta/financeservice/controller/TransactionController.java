package com.prorenta.financeservice.controller;

import com.prorenta.financeservice.model.dto.TransactionResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    /*
    TODO: создать метод getTransactions()
    - пользователь может получать только свои транзакции
    - не забыть про пагинацию
     */

    public ResponseEntity<TransactionResponseDto> createTransaction() {
        return null;
    }

}
