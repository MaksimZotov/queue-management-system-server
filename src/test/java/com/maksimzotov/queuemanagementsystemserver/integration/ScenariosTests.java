package com.maksimzotov.queuemanagementsystemserver.integration;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.ClientService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.Locale;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        classes = ScenariosTests.class
)
@TestPropertySource(
        locations="classpath:tests.yml"
)
public class ScenariosTests {

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private ClientService clientService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Mock
    private MessageSource messageSource;
    private Localizer localizer;

    private TokensResponse tokensResponseFirst;
    private TokensResponse tokensResponseSecond;

    @BeforeEach
    void setupLocalizer() {
        localizer = new Localizer(
                new Locale("ru"),
                messageSource
        );
    }

    @Test
    void testCreationOfServiceByAccountWithRights() {
        accountRepo.save(
               new AccountEntity(
                       1L,
                       "zotovm256@gmail.com",
                       "Test",
                       "Test",
                       passwordEncoder.encode("12345678"),
                       new Date()
               )
        );
        accountRepo.save(
                new AccountEntity(
                        2L,
                        "zotovmaksim1254@gmail.com",
                        "Test",
                        "Test",
                        passwordEncoder.encode("12345678"),
                        new Date()
                )
        );
    }
}
