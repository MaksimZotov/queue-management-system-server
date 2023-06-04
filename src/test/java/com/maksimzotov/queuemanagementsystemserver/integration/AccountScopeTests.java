package com.maksimzotov.queuemanagementsystemserver.integration;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RegistrationCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException;
import com.maksimzotov.queuemanagementsystemserver.integration.extension.PostgreSQLExtension;
import com.maksimzotov.queuemanagementsystemserver.model.account.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RegistrationCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(PostgreSQLExtension.class)
@TestPropertySource(
        properties = {"spring.config.location=classpath:application-tests.yml"}
)
@DirtiesContext
public class AccountScopeTests {

    private static final String PASSWORD = "12345678";
    private static final String FIRST_EMAIL = "zotovm256@gmail.com";
    private static final String SECOND_EMAIL = "zotovmaksim1254@gmail.com";

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    RegistrationCodeRepo registrationCodeRepo;

    @Mock
    private MessageSource messageSource;
    private Localizer localizer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TokensResponse tokens;

    @BeforeEach
    void beforeEach() {
        registrationCodeRepo.deleteAll();
        accountRepo.deleteAll();

        accountRepo.save(
                new AccountEntity(
                        null,
                        FIRST_EMAIL,
                        "Test",
                        "Test",
                        passwordEncoder.encode(PASSWORD),
                        new Date()
                )
        );

        tokens = assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        FIRST_EMAIL,
                        PASSWORD
                )
        ));

        localizer = new Localizer(
                new Locale("ru"),
                messageSource
        );
    }

    @Test
    void testConfirmNonExistingRegistrationCode() {
        assertThrows(DescriptionException.class, () -> accountService.confirmRegistrationCode(
                localizer,
                new ConfirmCodeRequest(
                        FIRST_EMAIL,
                        "1234"
                )
        ));
    }

    @Test
    void testConfirmExistingNonExpiredRegistrationCode() {
        registrationCodeRepo.save(
                new RegistrationCodeEntity(
                        FIRST_EMAIL,
                        1111
                )
        );
        assertThrows(DescriptionException.class, () -> accountService.confirmRegistrationCode(
                localizer,
                new ConfirmCodeRequest(
                        FIRST_EMAIL,
                        "0000"
                )
        ));

        Optional<RegistrationCodeEntity> registrationCodeBefore = registrationCodeRepo.findByEmail(FIRST_EMAIL);
        assertTrue(registrationCodeBefore.isPresent());

        assertDoesNotThrow(() -> accountService.confirmRegistrationCode(
                localizer,
                new ConfirmCodeRequest(
                        FIRST_EMAIL,
                        "1111"
                )
        ));

        Optional<RegistrationCodeEntity> registrationCodeAfter = registrationCodeRepo.findByEmail(FIRST_EMAIL);
        assertTrue(registrationCodeAfter.isEmpty());
    }

    @Test
    void testLoginIntoExistingAccount() {
        assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        FIRST_EMAIL,
                        PASSWORD
                )
        ));
    }

    @Test
    void testRefreshTokenOfExistingAccount() {
        assertDoesNotThrow(() -> accountService.refreshToken(
                tokens.getRefresh()
        ));
    }

    @Test
    void testSignup() {
        assertTrue(accountRepo.findByEmail(FIRST_EMAIL).isPresent());
        assertThrows(FieldsException.class, () -> accountService.signup(
                localizer,
                new SignupRequest(
                        FIRST_EMAIL,
                        "Maksim",
                        "Zotov",
                        PASSWORD,
                        PASSWORD
                )
        ));
        assertFalse(accountRepo.findByEmail(SECOND_EMAIL).isPresent());
        assertDoesNotThrow(() -> accountService.signup(
                localizer,
                new SignupRequest(
                        SECOND_EMAIL,
                        "Maksim",
                        "Zotov",
                        PASSWORD,
                        PASSWORD
                )
        ));
        assertTrue(accountRepo.findByEmail(SECOND_EMAIL).isPresent());
    }
}
