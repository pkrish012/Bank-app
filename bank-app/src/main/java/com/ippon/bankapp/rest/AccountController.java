package com.ippon.bankapp.rest;

import com.ippon.bankapp.domain.Account;
import com.ippon.bankapp.service.AccountService;
import com.ippon.bankapp.service.TransactionService;
import com.ippon.bankapp.service.dto.AccountDTO;
import com.ippon.bankapp.service.dto.TransactionDTO;
import com.ippon.bankapp.service.exception.DepositNotValidException;
import com.ippon.bankapp.domain.Deposit;
import com.ippon.bankapp.service.exception.TransactionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {

        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping("/account")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createAccount(@Valid @RequestBody AccountDTO newAccount) {
         return accountService.createAccount(newAccount);
    }

    @GetMapping("/account/lastName/{lastName}")
    public AccountDTO getAccountByLastName(@PathVariable String lastName) {
        log.info(lastName);
        return accountService.getAccount(lastName);
    }

    @GetMapping("/account/firstName/{firstName}")
    public AccountDTO getAccountByFirstName(@PathVariable String firstName) {
        log.info(firstName);
        return accountService.getAccountViaFirst(firstName);
    }

    @GetMapping("/account/identification/{id_number}")
    public AccountDTO getAccountByIdNum (@Valid @PathVariable("id_number") int id){
        log.info("Recieved ID of :" + id);
        return accountService.getAccountByIdNum(id);
    }

    @PostMapping("/account/{identification_number}/{deposit_ammount}")
    public AccountDTO depositHandlingInURL (@Valid @PathVariable("identification_number") int id, @PathVariable(name = "deposit_ammount") String ammount){
        return accountService.deposit(id, new BigDecimal(ammount));
    }

    @PostMapping("/deposit")
    public AccountDTO depositHandlingInJSON (@Valid @RequestBody Deposit deposit) throws DepositNotValidException {
        log.info("Received ID: {}, Received Amount to Deposit: {}", deposit.getId(), deposit.getAmount());
        return accountService.deposit(Integer.valueOf(deposit.getId()), new BigDecimal(deposit.getAmount()));
    }

    @PostMapping("/withdraw")
    public AccountDTO withdrawHandling (@Valid @RequestBody Deposit deposit) throws DepositNotValidException{
        log.info("Received ID: {}, Received Amount to Deposit: {}", deposit.getId(), deposit.getAmount());
        return accountService.withdraw(Integer.valueOf(deposit.getId()), new BigDecimal(deposit.getAmount()));
    }

    @PostMapping("wire_transfers")
    public void wireTransferHandling(@Valid @RequestBody Deposit deposit) throws DepositNotValidException{
        log.info("Received IDs:  {}, {}, Received Amount to Transfer between two accounts: {}", deposit.getId(), deposit.getId2(), deposit.getAmount());
        accountService.wireTransferFromAccount1ToAccount2(Integer.valueOf(deposit.getId()),Integer.valueOf(deposit.getId2()),new BigDecimal(deposit.getAmount()));
    }

    @PostMapping("/history")
    public ArrayList<TransactionDTO> getLastTransactions (@Valid @RequestBody AccountDTO account) throws TransactionNotFoundException {
        return transactionService.findAllTransactionsByAccount(account);
    }

}
