package com.ippon.bankapp.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ippon.bankapp.domain.Account;
import com.ippon.bankapp.rest.errors.RestErrorHandler;
import com.ippon.bankapp.service.AccountService;
import com.ippon.bankapp.service.TransactionService;
import com.ippon.bankapp.service.dto.AccountDTO;
import com.ippon.bankapp.service.dto.TransactionDTO;
import com.ippon.bankapp.service.exception.AccountLastNameExistsException;
import com.ippon.bankapp.service.exception.AccountNotFoundException;
import com.ippon.bankapp.service.exception.DepositNotValidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {AccountController.class, RestErrorHandler.class})
class AccountControllerTest {

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private RestErrorHandler restErrorHandler;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void before() {
        AccountController subject = new AccountController(accountService, transactionService);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(subject)
                .setControllerAdvice(restErrorHandler)
                .build();
    }

    @Test
    public void testAccountRetrieval_AccountExistsFirstName() throws Exception {
        given(accountService.getAccountViaFirst("Ben"))
                .willReturn(new AccountDTO()
                        .lastName("Scott")
                        .firstName("Ben"));

        mockMvc
                .perform(get("/api/account/firstName/Ben"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ben"))
                .andExpect(jsonPath("$.lastName").value("Scott"));
    }

    @Test
    public void testAccountRetrieval_AccountExistsLastName() throws Exception {
        given(accountService.getAccount("Scott"))
                .willReturn(new AccountDTO()
                        .lastName("Scott")
                        .firstName("Ben"));

        mockMvc
                .perform(get("/api/account/lastName/Scott"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ben"))
                .andExpect(jsonPath("$.lastName").value("Scott"));
    }

    @Test
    public void testAccountRetrieval_AccountDoesNotExistFirstName() throws Exception {
        given(accountService.getAccountViaFirst("Ben"))
                .willThrow(new AccountNotFoundException());

        String errorMessage = mockMvc
                .perform(get("/api/account/firstName/Ben"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getErrorMessage();

        assertThat(errorMessage, is("Account not found"));
    }

    @Test
    public void testAccountRetrieval_AccountDoesNotExistLastName() throws Exception {
        given(accountService.getAccount("Scott"))
                .willThrow(new AccountNotFoundException());

        String errorMessage = mockMvc
                .perform(get("/api/account/lastName/Scott"))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getErrorMessage();

        assertThat(errorMessage, is("Account not found"));
    }

    @Test
    public void testAccountRetrieval_AccountExistsId() throws Exception {
        given(accountService.getAccountByIdNum(1))
                .willReturn(new AccountDTO()
                        .firstName("Ben")
                        .lastName("Scott"));

        mockMvc
                .perform(get("/api/account/identification/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ben"))
                .andExpect(jsonPath("$.lastName").value("Scott"));
    }

    @Test
    public void testDeposit_givenValidDeposit() throws Exception {
        given(accountService.deposit(1,new BigDecimal("2000")))
                .willReturn(new AccountDTO().firstName("Ben").lastName("Scott").balance(new BigDecimal("2000")));
    }

    @Test
    public void testAccountDepositHandlingFailureWithNegative() throws Exception {
        given(accountService.deposit(1, new BigDecimal(-2000))).willThrow(new DepositNotValidException());

        String errorMessage = mockMvc
                .perform(get("/api/deposit"))
                .andReturn()
                .getResponse()
                .getErrorMessage();
    }

    @Test
    public void testAccountDepositHandlingFailureWithTimeslot() throws Exception {
        given(accountService.deposit(1, new BigDecimal(2500))).willReturn(new AccountDTO()
                .firstName("Ben")
                .lastName("Scott")
                .balance(new BigDecimal(2500)));
        given(accountService.deposit(1, new BigDecimal(2500))).willReturn(new AccountDTO()
                .firstName("Ben")
                .lastName("Scott")
                .balance(new BigDecimal(5000)));
        given(accountService.deposit(1, new BigDecimal(2500))).willThrow(new DepositNotValidException());

        String errorMessage = mockMvc
                .perform(get("/api/deposit"))
                .andReturn()
                .getResponse()
                .getErrorMessage();
    }

    @Test
    public void testAccount_givenValidWithdrawAmount() throws Exception {
        given(accountService.deposit(1, new BigDecimal(2500))).willReturn(new AccountDTO()
                .firstName("Ben")
                .lastName("Scott")
                .balance(new BigDecimal(2500)));
        given(accountService.withdraw(1, new BigDecimal(2000))).willReturn(new AccountDTO()
                .firstName("Ben")
                .lastName("Scott")
                .balance(new BigDecimal(500)));
    }

    @Test
    public void testAccountWithdrawHandlingFailureWithNegative() throws Exception {
        given(accountService.withdraw(1, new BigDecimal(-2000))).willThrow(new DepositNotValidException());

        String errorMessage = mockMvc
                .perform(get("/api/deposit"))
                .andReturn()
                .getResponse()
                .getErrorMessage();
    }

    @Test
    public void testTransactionalList() throws Exception {

        accountService.deposit(1, new BigDecimal(1));
        accountService.deposit(1, new BigDecimal(1));
        accountService.withdraw(1, new BigDecimal(1));
        accountService.deposit(1, new BigDecimal(1));
        accountService.deposit(1, new BigDecimal(1));
        accountService.withdraw(1, new BigDecimal(1));
        accountService.deposit(1, new BigDecimal(1));


        String message1 = "Deposit of " + 1 + "$";
        String message2 = "Withdrawal of " + 1 + "$";
        TransactionDTO depositMessage = new TransactionDTO(message1, new BigDecimal(1));
        TransactionDTO withdraw = new TransactionDTO(message2, new BigDecimal(1));
        ArrayList<TransactionDTO> transactionDTOS = new ArrayList<>();
        transactionDTOS.add(depositMessage);
        transactionDTOS.add(withdraw);
        transactionDTOS.add(depositMessage);
        transactionDTOS.add(depositMessage);
        transactionDTOS.add(withdraw);
        transactionDTOS.add(depositMessage);
        transactionDTOS.add(depositMessage);
        ArrayList<TransactionDTO> returnable = transactionService.findAllTransactionsByAccount(accountService.getAccountByIdNum(1));
        given(returnable).willReturn(transactionDTOS);
    }

    @Test
    public void testTransfer(){
        given(accountService.deposit(1, new BigDecimal(2500))).willReturn(new AccountDTO()
                .firstName("Ben")
                .lastName("Scott")
                .balance(new BigDecimal(2500)));

        AccountDTO newAccount = new AccountDTO()
                .firstName("Bill")
                .lastName("Bill");

        given(accountService.createAccount(newAccount))
                .willReturn(new AccountDTO()
                        .lastName("Bill")
                        .firstName("Bill")
                        .balance(BigDecimal.ZERO)
                        .notificationPreference("email"));

        given(accountService.deposit(2, new BigDecimal(2500))).willReturn(new AccountDTO()
                .firstName("Bill")
                .lastName("Bill")
                .balance(new BigDecimal(2500)));

        accountService.wireTransferFromAccount1ToAccount2(1,2,new BigDecimal(500));

        given(accountService.getAccountByIdNum(1)).willReturn(new AccountDTO()
                .firstName("Ben")
                .lastName("Scott")
                .balance(new BigDecimal(2000)));

        given(accountService.getAccountByIdNum(2)).willReturn(new AccountDTO()
                .firstName("Bill")
                .lastName("Bill")
                .balance(new BigDecimal(3000)));



    }

    @Test
    public void testCreateAccount_requestValid() throws Exception {
        AccountDTO newAccount = new AccountDTO()
                .firstName("Ben")
                .lastName("Scott");

        given(accountService.createAccount(newAccount))
                .willReturn(new AccountDTO()
                        .lastName("Scott")
                        .firstName("Ben")
                        .balance(BigDecimal.ZERO)
                        .notificationPreference("email"));

        mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Ben"))
                .andExpect(jsonPath("$.lastName").value("Scott"))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.notificationPreference").value("email"));

    }

    @Test
    public void testCreateAccount_missingFirstName() throws Exception {

        AccountDTO newAccount = new AccountDTO()
                .lastName("Scott");

        ResultActions mvcResult = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateAccount_missingLastName() throws Exception {
        AccountDTO newAccount = new AccountDTO()
                .firstName("Ben");

        ResultActions mvcResult = mockMvc
                .perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateAccount_lastNameExists_throwsException() throws Exception {
        AccountDTO newAccount = new AccountDTO()
                .firstName("Ben")
                .lastName("Scott");

        given(accountService.createAccount(newAccount))
                .willThrow(new AccountLastNameExistsException());

        mockMvc
                .perform(post("/api/account/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newAccount)))
                .andExpect(status().isConflict());
    }
}
