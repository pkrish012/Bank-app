package com.ippon.bankapp.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int transactionId;

    @Column(name = "transaction_Type")
    private String transactionType;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    private Account account;

    public Transaction(Account account, String transactionType, BigDecimal amount) {
        this.transactionType = transactionType;
        this.account = account;
        this.timestamp = account.getLastUpdatedDate();
        this.amount = amount;
    }

    public Transaction() {
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return transactionId == that.transactionId && Objects.equals(transactionType, that.transactionType) && Objects.equals(amount, that.amount) && Objects.equals(timestamp, that.timestamp) && Objects.equals(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionType, transactionId, amount, timestamp, account);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionType='" + transactionType + '\'' +
                ", id=" + transactionId +
                ", balance=" + amount +
                ", timestamp=" + timestamp +
                ", account=" + account +
                '}';
    }
}
