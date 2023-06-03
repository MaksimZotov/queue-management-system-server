package com.maksimzotov.queuemanagementsystemserver.integration.services;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RegistrationCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.integration.util.PostgreSQLExtension;
import com.maksimzotov.queuemanagementsystemserver.model.account.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.LocationService;
import com.maksimzotov.queuemanagementsystemserver.service.RightsService;
import com.maksimzotov.queuemanagementsystemserver.service.ServiceService;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(PostgreSQLExtension.class)
@TestPropertySource(
        properties = {"spring.config.location=classpath:application-tests.yml"}
)
@DirtiesContext
public class AccountServiceTests {

    @Autowired
    private ServiceService serviceService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RightsService rightsService;

    @Autowired
    private RightsRepo rightsRepo;
    @Autowired
    private AccountRepo accountRepo;
    @Autowired
    private ServiceRepo serviceRepo;
    @Autowired
    private LocationRepo locationRepo;
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
        rightsRepo.deleteAll();
        serviceRepo.deleteAll();
        locationRepo.deleteAll();
        registrationCodeRepo.deleteAll();
        accountRepo.deleteAll();

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

        tokens = assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        "zotovm256@gmail.com" ,
                        "12345678"
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
                        "zotovm256@gmail.com",
                        "1234"
                )
        ));
    }

    @Test
    void testConfirmExistingNonExpiredRegistrationCode() {
        registrationCodeRepo.save(
                new RegistrationCodeEntity(
                        "zotovm256@gmail.com",
                        1111
                )
        );
        assertThrows(DescriptionException.class, () -> accountService.confirmRegistrationCode(
                localizer,
                new ConfirmCodeRequest(
                        "zotovm256@gmail.com",
                        "0000"
                )
        ));
        assertDoesNotThrow(() -> accountService.confirmRegistrationCode(
                localizer,
                new ConfirmCodeRequest(
                        "zotovm256@gmail.com",
                        "1111"
                )
        ));
    }

    @Test
    void testSignupIntoExistingAccount() {
        assertDoesNotThrow(() -> accountService.login(
                localizer,
                new LoginRequest(
                        "zotovm256@gmail.com",
                        "12345678"
                )
        ));
    }

    @Test
    void testRefreshTokenOfExistingAccount() {
        assertDoesNotThrow(() -> accountService.refreshToken(
                tokens.getRefresh()
        ));
    }
}
