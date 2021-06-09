package com.ippon.bankapp.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int accountId;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "notification_preference")
    private String notificationPreference;

    @Column(name= "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    @OneToMany(
            mappedBy = "account",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<Transaction> transactions =  new ArrayList<>();

    public Account() {}

    public Account(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.balance = BigDecimal.ZERO;
        this.lastUpdatedDate = LocalDateTime.now();
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal amount) {
        this.balance = amount;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getNotificationPreference() {
        return notificationPreference;
    }

    public void setNotificationPreference(String notificationPreference) {
        this.notificationPreference = notificationPreference;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return accountId == account.accountId && Objects.equals(balance, account.balance) && Objects.equals(firstName, account.firstName) && Objects.equals(lastName, account.lastName) && Objects.equals(notificationPreference, account.notificationPreference) && Objects.equals(lastUpdatedDate, account.lastUpdatedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, balance, firstName, lastName, notificationPreference, lastUpdatedDate);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + accountId +
                ", balance=" + balance +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", notificationPreference='" + notificationPreference + '\'' +
                ", lastUpdatedDate=" + lastUpdatedDate +
                '}';
    }
}
