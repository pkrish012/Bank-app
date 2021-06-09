package com.ippon.bankapp.service;

import com.ippon.bankapp.domain.Account;
import com.ippon.bankapp.domain.Transaction;
import com.ippon.bankapp.repository.AccountRepository;
import com.ippon.bankapp.repository.TransactionRepository;
import com.ippon.bankapp.service.dto.AccountDTO;
import com.ippon.bankapp.service.dto.TransactionDTO;
import com.ippon.bankapp.service.exception.AccountNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class TransactionService {
    private TransactionRepository transactionRepository;
    private AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }


    public ArrayList<TransactionDTO> findAllTransactionsByAccount(AccountDTO account){
        Account client = getAccountViaFirstWithoutDTO(account.getFirstName());
        ArrayList<Transaction> transactions = transactionRepository.findAllByAccount(client);
        ArrayList<TransactionDTO> previousTransactions = new ArrayList<>();
        if(transactions.size() < 10){
            for(int i = transactions.size() - 1; i >= 0; i--){
                previousTransactions.add(mapTransactionToDTO(transactions.get(i)));
            }
        }
        else{
            int startIndex = transactions.size() -1;
            int endIndex = startIndex - 9;
            for(int i = startIndex; i >= endIndex; i--){
                previousTransactions.add(mapTransactionToDTO(transactions.get(i)));
            }
        }

        return previousTransactions;
    }

    protected TransactionDTO mapTransactionToDTO(Transaction transaction){
        return new TransactionDTO(transaction.getTransactionType(),transaction.getAmount());
    }

    public Account getAccountViaFirstWithoutDTO(String firstName){
        return accountRepository
                .findByFirstName(firstName)
                .orElseThrow(AccountNotFoundException::new);
    }
}
