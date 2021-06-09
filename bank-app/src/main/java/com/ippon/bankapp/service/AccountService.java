package com.ippon.bankapp.service;

import com.ippon.bankapp.domain.Account;
import com.ippon.bankapp.domain.Transaction;
import com.ippon.bankapp.repository.AccountRepository;
import com.ippon.bankapp.repository.TransactionRepository;
import com.ippon.bankapp.service.dto.AccountDTO;

import com.ippon.bankapp.service.exception.AccountLastNameExistsException;
import com.ippon.bankapp.service.exception.AccountNotFoundException;
import com.ippon.bankapp.service.exception.DepositNotValidException;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;




@Service
public class AccountService {

    private AccountRepository accountRepository;
    private NotificationFactory notificationFactory;
    private TransactionRepository transactionRepository;
    private BigDecimal accountDepositDailyLimit = new BigDecimal(5000);
    private BigDecimal dailyDeposited = new BigDecimal(0);

    private LocalDateTime startTime = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 0,0,0);
    private LocalDateTime endTime = LocalDateTime.of(LocalDateTime.now().getYear(), LocalDateTime.now().getMonth(), LocalDateTime.now().getDayOfMonth(), 23,59,59);


    public AccountService(AccountRepository accountRepository, NotificationFactory notificationFactory, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.notificationFactory = notificationFactory;
        this.transactionRepository = transactionRepository;
    }

    public AccountDTO createAccount(AccountDTO newAccount) {
        validateLastNameUnique(newAccount.getLastName());
        Account account = new Account(newAccount.getFirstName(), newAccount.getLastName());
        account.setNotificationPreference(notificationFactory
                .getDefaultNotification()
                .getName());
        Account save = accountRepository.save(account);

        notificationFactory
                .getPreferredService(save.getNotificationPreference())
                .orElseGet(notificationFactory::getDefaultNotification)
                .sendMessage("bank",
                        account.getLastName(),
                        "Account Created",
                        "Welcome aboard!");

        return mapAccountToDTO(save);
    }

    public AccountDTO getAccount(String lastName) {
        Account account = accountRepository
                .findByLastName(lastName)
                .orElseThrow(AccountNotFoundException::new);
        return mapAccountToDTO(account);
    }

    public AccountDTO getAccountViaFirst(String firstName) {
        Account account = accountRepository
                .findByFirstName(firstName)
                .orElseThrow(AccountNotFoundException::new);

        return mapAccountToDTO(account);
    }

    public AccountDTO getAccountByIdNum(int id) {
        Account account = accountRepository.findByAccountId(id).orElseThrow(AccountNotFoundException::new);
        return mapAccountToDTO(account);
    }

    public AccountDTO deposit(int id, BigDecimal depositAmount) throws DepositNotValidException {
        Account account = accountRepository.findByAccountId(id).orElseThrow(AccountNotFoundException::new);
        AccountDTO dto = new AccountDTO();
        BigDecimal newBal = new BigDecimal("0");

        LocalDateTime lastUpdateTime = account.getLastUpdatedDate();


        if(LocalDateTime.now().isAfter(endTime)){
            dailyDeposited = new BigDecimal("0");
            accountDepositDailyLimit = new BigDecimal("5000");
            dto = depositHelper(account, dto, newBal, id,depositAmount);
            LocalDateTime copy = endTime;
            startTime = endTime;
            endTime = copy.plusDays(1);
        }
        else if (lastUpdateTime.isAfter(startTime) && lastUpdateTime.isBefore(endTime) && dailyDeposited.doubleValue() <= 5000 ){
            dto = depositHelper(account, dto, newBal, id, depositAmount);
        }
        else{
            throw new DepositNotValidException();
        }
        return dto;

    }

    @NotNull
    public AccountDTO depositHelper(Account account, AccountDTO dto, BigDecimal newBal, int id, BigDecimal depositAmount) throws DepositNotValidException{
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DepositNotValidException();
        } else {
            if (accountDepositDailyLimit.subtract(depositAmount).doubleValue() < 0) {
                throw new DepositNotValidException();
            } else {
                newBal = account.getBalance().add(depositAmount);
                accountDepositDailyLimit = accountDepositDailyLimit.subtract(depositAmount);
                account.setBalance(newBal);
                account.setLastUpdatedDate(LocalDateTime.now());

                String message = "Deposit of " + depositAmount + "$";
                Transaction transaction = new Transaction(account, message , depositAmount);
                account.getTransactions().add(transaction);
                //transactionRepository.save(transaction);

                dailyDeposited.add(depositAmount);
                dto = mapAccountToDTO(account);
                accountRepository.save(account);
            }
        }
        return dto;
    }

    public AccountDTO withdraw(int id, BigDecimal depositAmount) throws DepositNotValidException {
        Account account = accountRepository.findByAccountId(id).orElseThrow(AccountNotFoundException::new);
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DepositNotValidException();
        } else {
            if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                throw new DepositNotValidException();
            } else {
                BigDecimal newBal = account.getBalance().subtract(depositAmount);
                account.setBalance(newBal);
                Transaction transaction = new Transaction(account, "Withdrawal of " + depositAmount + "$", depositAmount);
                //transactionRepository.save(transaction);

                AccountDTO dto = mapAccountToDTO(account);
                accountRepository.save(account);
                return dto;
            }
        }
    }

    public void wireTransferFromAccount1ToAccount2(int id1, int id2, BigDecimal depositAmount) throws DepositNotValidException{
        AccountDTO account1 = withdrawHelperForWireTransfer(id1, id2, depositAmount);
        AccountDTO account2 = depositHelperForWireTransfer(id2, id1, depositAmount);
    }

    @NotNull
    public AccountDTO depositHelperForWireTransfer(int id2, int id1, BigDecimal depositAmount) throws DepositNotValidException {
        Account account = accountRepository.findByAccountId(id2).orElseThrow(AccountNotFoundException::new);
        BigDecimal newBal = new BigDecimal("0");
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DepositNotValidException();
        } else {
            if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                throw new DepositNotValidException();
            } else {
                newBal = account.getBalance().add(depositAmount);
                account.setBalance(newBal);
                String message = "Wire Transfer of " + depositAmount + "$ was received from "
                        + accountRepository.findByAccountId(id1).get().getFirstName();
                Transaction transaction = new Transaction(account, message, depositAmount);
                transactionRepository.save(transaction);

                AccountDTO dto = mapAccountToDTO(account);
                accountRepository.save(account);
                return dto;
            }
        }
    }

    public AccountDTO withdrawHelperForWireTransfer(int id1, int id2, BigDecimal depositAmount) throws DepositNotValidException {
        Account account = accountRepository.findByAccountId(id1).orElseThrow(AccountNotFoundException::new);
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DepositNotValidException();
        } else {
            if (account.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                throw new DepositNotValidException();
            } else {
                BigDecimal newBal = account.getBalance().subtract(depositAmount);
                account.setBalance(newBal);
                String message = "Wire Transfer of " + depositAmount + "$ was sent to " + accountRepository.findByAccountId(id2).get().getFirstName();
                Transaction transaction = new Transaction(account, message, depositAmount);
                transactionRepository.save(transaction);

                AccountDTO dto = mapAccountToDTO(account);
                accountRepository.save(account);
                return dto;
            }
        }
    }


    private void validateLastNameUnique(String lastName) {
        accountRepository
                .findByLastName(lastName)
                .ifPresent(t -> {
                    throw new AccountLastNameExistsException();
                });
    }

    protected AccountDTO mapAccountToDTO(Account account) {
        return new AccountDTO()
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .balance(account.getBalance())
                .notificationPreference(account.getNotificationPreference());
    }





}
