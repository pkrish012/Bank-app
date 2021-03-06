package com.ippon.bankapp.repository;


import com.ippon.bankapp.domain.Account;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountRepository  extends CrudRepository<Account, String> {

    Optional<Account> findByLastName(String lastName);

    Optional<Account> findByFirstName(String firstName);

    Optional<Account> findByAccountId(int id);
}
