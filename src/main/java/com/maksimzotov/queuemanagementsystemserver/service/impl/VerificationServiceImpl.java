package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.maksimzotov.queuemanagementsystemserver.QueueManagementSystemServerApplication;
import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RegistrationCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenIsMissingException;
import com.maksimzotov.queuemanagementsystemserver.model.verification.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RegistrationCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.service.CleanerService;
import com.maksimzotov.queuemanagementsystemserver.service.MailService;
import com.maksimzotov.queuemanagementsystemserver.service.VerificationService;
import com.maksimzotov.queuemanagementsystemserver.util.Util;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class VerificationServiceImpl implements VerificationService {

    private final MailService mailService;
    private final AccountRepo accountRepo;
    private final RegistrationCodeRepo registrationCodeRepo;
    private final CleanerService cleanerService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final String secret;
    private final Long accessTokenExpiration;
    private final Long refreshTokenExpiration;
    private final Integer confirmationTimeInSeconds;

    public VerificationServiceImpl(
            MailService mailService,
            AccountRepo accountRepo,
            RegistrationCodeRepo registrationCodeRepo,
            CleanerService cleanerService, AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            @Value("${app.tokens.secret}") String secret,
            @Value("${app.tokens.access.expiration}") Long accessTokenExpiration,
            @Value("${app.tokens.refresh.expiration}") Long refreshTokenExpiration,
            @Value("${app.registration.confirmationtime.registration}") Integer confirmationTimeInSeconds
    ) {
        this.mailService = mailService;
        this.accountRepo = accountRepo;
        this.registrationCodeRepo = registrationCodeRepo;
        this.cleanerService = cleanerService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.confirmationTimeInSeconds = confirmationTimeInSeconds;
    }

    @Override
    public void signup(SignupRequest signupRequest) throws FieldsException {
        Map<String, String> fieldsErrors = new HashMap<>();
        if (signupRequest.getUsername().isEmpty()) {
            fieldsErrors.put(FieldsException.USERNAME, "Логин не может быть пустым");
        }
        if (signupRequest.getUsername().length() > 64) {
            fieldsErrors.put(FieldsException.USERNAME, "Логин должнен содержать меньше 64 символов");
        }
        if (signupRequest.getPassword().length() < 8) {
            fieldsErrors.put(FieldsException.PASSWORD, "Пароль должнен содержать больше 8 символов");
        }
        if (signupRequest.getPassword().length() > 64) {
            fieldsErrors.put(FieldsException.PASSWORD, "Пароль должнен содержать меньше 64 символов");
        }
        if (!signupRequest.getPassword().equals(signupRequest.getRepeatPassword())) {
            fieldsErrors.put(FieldsException.REPEAT_PASSWORD, "Пароли не совпадают");
        }
        if (signupRequest.getFirstName().isEmpty()) {
            fieldsErrors.put(FieldsException.FIRST_NAME, "Имя не может быть пустым");
        }
        if (signupRequest.getFirstName().length() > 64) {
            fieldsErrors.put(FieldsException.FIRST_NAME, "Имя должно содержать меньше 64 символов");
        }
        if (signupRequest.getLastName().isEmpty()) {
            fieldsErrors.put(FieldsException.LAST_NAME, "Фамилия не может быть пустой");
        }
        if (signupRequest.getLastName().length() > 64) {
            fieldsErrors.put(FieldsException.LAST_NAME, "Фамилия должна содержать меньше 64 символов");
        }
        if (!Util.emailMatches(signupRequest.getEmail())) {
            fieldsErrors.put(FieldsException.EMAIL, "Некорректная почта");
        }
        if (!fieldsErrors.isEmpty()) {
            throw new FieldsException(fieldsErrors);
        }
        if (accountRepo.existsByUsername(signupRequest.getUsername())) {
            if (registrationCodeRepo.existsById(signupRequest.getUsername())) {
                fieldsErrors.put(
                        FieldsException.USERNAME,
                        "Логин " + signupRequest.getUsername() +
                                " зарезервирован. Пожалуйста, попробуете позже"
                );
            } else {
                fieldsErrors.put(
                        FieldsException.USERNAME,
                        "Пользователь с уникальным именем " + signupRequest.getUsername() + " уже существует"
                );
            }
        }
        if (accountRepo.existsByEmail(signupRequest.getEmail())) {
            fieldsErrors.put(
                    FieldsException.EMAIL,
                    "Пользователь с почтой " + signupRequest.getEmail() +
                            " уже существует"

            );
        }
        if (!fieldsErrors.isEmpty()) {
            throw new FieldsException(fieldsErrors);
        }

        int code = new Random().nextInt(9000) + 1000;
        AccountEntity account = new AccountEntity(
                null,
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                signupRequest.getFirstName(),
                signupRequest.getLastName(),
                passwordEncoder.encode(signupRequest.getPassword())
        );

        accountRepo.save(account);
        registrationCodeRepo.save(
                new RegistrationCodeEntity(
                        account.getUsername(),
                        Integer.toString(code)
                )
        );

        mailService.send(
                signupRequest.getEmail(),
                "Подтверждение регистрации",
                "Код для подтверждения регистрации: " + code
        );

        QueueManagementSystemServerApplication.scheduledExecutorService.schedule(() ->
                cleanerService.deleteNonActivatedUser(
                        account.getUsername()
                ),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void confirmRegistrationCode(ConfirmCodeRequest confirmCodeRequest) throws DescriptionException {
        if (confirmCodeRequest.getCode().length() != 4) {
            throw new DescriptionException("Код должен содержать 4 символа");
        }
        if (!registrationCodeRepo.existsByUsername(confirmCodeRequest.getUsername())) {
            throw new DescriptionException("Время действия кода истекло");
        }
        registrationCodeRepo.deleteById(confirmCodeRequest.getUsername());
    }

    @Override
    public TokensResponse login(LoginRequest loginRequest) throws FieldsException, DescriptionException {
        Map<String, String> fieldsErrors = new HashMap<>();
        if (loginRequest.getUsername().isEmpty()) {
            fieldsErrors.put(FieldsException.USERNAME, "Логин не может быть пустым");
        }
        if (loginRequest.getPassword().length() < 8) {
            fieldsErrors.put(FieldsException.PASSWORD, "Пароль должнен содержать больше 8 символов");
        }
        if (loginRequest.getPassword().length() > 64) {
            fieldsErrors.put(FieldsException.PASSWORD, "Пароль должнен содержать меньше 64 символов");
        }
        if (!fieldsErrors.isEmpty()) {
            throw new FieldsException(fieldsErrors);
        }
        Optional<AccountEntity> account = accountRepo.findByUsername(loginRequest.getUsername());
        if (account.isEmpty()) {
            fieldsErrors.put(FieldsException.USERNAME, "Пользователя с уникальным именем " + loginRequest.getUsername() + " не существует");
            throw new FieldsException(fieldsErrors);
        }
        AccountEntity accountEntity = account.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), accountEntity.getPassword())) {
            fieldsErrors.put(FieldsException.PASSWORD, "Неверный пароль");
            throw new FieldsException(fieldsErrors);
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception ex) {
            throw new DescriptionException("Не удалось авторизоваться");
        }

        User user = (User)authentication.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());

        String access = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .sign(algorithm);

        String refresh = JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .sign(algorithm);

        return new TokensResponse(access, refresh, user.getUsername());
    }

    @Override
    public TokensResponse refreshToken(String refreshToken) throws RefreshTokenIsMissingException {
        if (refreshToken.startsWith("Bearer ")) {
            String refreshTokenSrc = refreshToken.substring("Bearer ".length());
            Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(refreshTokenSrc);
            String username = decodedJWT.getSubject();

            Optional<AccountEntity> account = accountRepo.findByUsername(username);
            if (account.isEmpty()) {
                throw new RefreshTokenIsMissingException();
            }
            AccountEntity accountEntity = account.get();

            String accessToken = JWT.create()
                    .withSubject(accountEntity.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                    .sign(algorithm);

            return new TokensResponse(accessToken, refreshTokenSrc, username);
        } else {
            throw new RefreshTokenIsMissingException();
        }
    }
}
