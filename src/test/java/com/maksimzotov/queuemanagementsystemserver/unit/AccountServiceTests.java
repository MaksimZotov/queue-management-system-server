package com.maksimzotov.queuemanagementsystemserver.unit;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenFailedException;
import com.maksimzotov.queuemanagementsystemserver.model.account.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RegistrationCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.service.MailService;
import com.maksimzotov.queuemanagementsystemserver.service.impl.AccountServiceImpl;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;
import java.util.Set;

import static com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTests {

    @Mock
    private MailService mailService;
    @Mock
    private AccountRepo accountRepo;
    @Mock
    private RegistrationCodeRepo registrationCodeRepo;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AccountServiceImpl accountServiceImpl;

    @Mock
    private MessageSource messageSource;
    private Localizer localizer;

    @BeforeEach
    void setupLocalizer() {
        localizer = new Localizer(
                new Locale("ru"),
                messageSource
        );
    }

    @Test
    void testSignup() {
        FieldsException exception = assertThrows(FieldsException.class, () -> accountServiceImpl.signup(
                localizer,
                new SignupRequest(
                        "",
                        "",
                        "",
                        "",
                        ""
                )
        ));
        assertEquals(
                Set.of(EMAIL, FIRST_NAME, LAST_NAME, PASSWORD),
                exception.getErrors().keySet()
        );

        exception = assertThrows(FieldsException.class, () -> accountServiceImpl.signup(
                localizer,
                new SignupRequest(
                        "zotovm256gmail.com",
                        "Maksim",
                        "Zotov",
                        "12345678",
                        "123456789"
                )
        ));
        assertEquals(
                Set.of(EMAIL, REPEAT_PASSWORD),
                exception.getErrors().keySet()
        );

        exception = assertThrows(FieldsException.class, () -> accountServiceImpl.signup(
                localizer,
                new SignupRequest(
                        "zotovm256@gmail.com",
                        "Maksim",
                        "Zotov",
                        "1234567",
                        "1234567"
                )
        ));
        assertEquals(
                Set.of(PASSWORD),
                exception.getErrors().keySet()
        );

        exception = assertThrows(FieldsException.class, () -> accountServiceImpl.signup(
                localizer,
                new SignupRequest(
                        "zotovm256@gmail.com",
                        "Maksim",
                        "Zotov",
                        "64".repeat(33),
                        "12345678"
                )
        ));
        assertEquals(
                Set.of(PASSWORD, REPEAT_PASSWORD),
                exception.getErrors().keySet()
        );

        exception = assertThrows(FieldsException.class, () -> accountServiceImpl.signup(
                localizer,
                new SignupRequest(
                        "zotovm256@gmail.com",
                        "Maksim",
                        "Zotov",
                        "123 5678",
                        "12345678"
                )
        ));
        assertEquals(
                Set.of(PASSWORD, REPEAT_PASSWORD),
                exception.getErrors().keySet()
        );

        assertDoesNotThrow(() -> accountServiceImpl.signup(
                localizer,
                new SignupRequest(
                        "zotovm256@gmail.com",
                        "Maksim",
                        "Zotov",
                        "12345678",
                        "12345678"
                )
        ));
        assertDoesNotThrow(() -> accountServiceImpl.signup(
                localizer,
                new SignupRequest(
                        "zotovm256@gmail.com",
                        "Maksim",
                        "Zotov",
                        "123456789",
                        "123456789"
                )
        ));
    }

    @Test
    void testLogin() {
        FieldsException exception = assertThrows(FieldsException.class, () -> accountServiceImpl.login(
                localizer,
                new LoginRequest(
                        "",
                        ""
                )
        ));
        assertEquals(
                Set.of(EMAIL, PASSWORD),
                exception.getErrors().keySet()
        );

        exception = assertThrows(FieldsException.class, () -> accountServiceImpl.login(
                localizer,
                new LoginRequest(
                        "zotovm256@gmail.com",
                        "1234567"
                )
        ));
        assertEquals(
                Set.of(PASSWORD),
                exception.getErrors().keySet()
        );

        exception = assertThrows(FieldsException.class, () -> accountServiceImpl.login(
                localizer,
                new LoginRequest(
                        "zotovm256gmail.com",
                        "12345678"
                )
        ));
        assertEquals(
                Set.of(EMAIL),
                exception.getErrors().keySet()
        );

        exception = assertThrows(FieldsException.class, () -> accountServiceImpl.login(
                localizer,
                new LoginRequest(
                        "zotovm@256gmail.com",
                        "64".repeat(33)
                )
        ));
        assertEquals(
                Set.of(PASSWORD),
                exception.getErrors().keySet()
        );

        exception = assertThrows(FieldsException.class, () -> accountServiceImpl.login(
                localizer,
                new LoginRequest(
                        "zotovm@256gmail.com",
                        "123 5678"
                )
        ));
        assertEquals(
                Set.of(PASSWORD),
                exception.getErrors().keySet()
        );

        exception = assertThrows(FieldsException.class, () -> accountServiceImpl.login(
                localizer,
                new LoginRequest(
                        "zotovm@256gmail.com",
                        "12345678"
                )
        ));
        assertEquals(
                Set.of(EMAIL),
                exception.getErrors().keySet()
        );
    }

    @Test
    void testGetEmail() {
        assertThrows(AccountIsNotAuthorizedException.class, () -> accountServiceImpl.getEmail(
                null
        ));
        assertThrows(AccountIsNotAuthorizedException.class, () -> accountServiceImpl.getEmail(
                "Invalid token"
        ));
    }

    @Test
    void testGetConfirmRegistrationCode() {
        assertThrows(DescriptionException.class, () -> accountServiceImpl.confirmRegistrationCode(
                localizer,
                new ConfirmCodeRequest(
                        "",
                        ""
                )
        ));
        assertThrows(DescriptionException.class, () -> accountServiceImpl.confirmRegistrationCode(
                localizer,
                new ConfirmCodeRequest(
                        "",
                        "1234"
                )
        ));
    }

    @Test
    void testRefreshToken() {
        assertThrows(RefreshTokenFailedException.class, () -> accountServiceImpl.refreshToken(
                null
        ));
        assertThrows(RefreshTokenFailedException.class, () -> accountServiceImpl.refreshToken(
                ""
        ));
    }
}
