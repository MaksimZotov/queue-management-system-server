package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RegistrationCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenFailedException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.verification.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RegistrationCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.CleanerService;
import com.maksimzotov.queuemanagementsystemserver.service.DelayedJobService;
import com.maksimzotov.queuemanagementsystemserver.service.MailService;
import com.maksimzotov.queuemanagementsystemserver.util.CodeGenerator;
import com.maksimzotov.queuemanagementsystemserver.util.EmailChecker;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final MailService mailService;
    private final DelayedJobService delayedJobService;
    private final AccountRepo accountRepo;
    private final RegistrationCodeRepo registrationCodeRepo;
    private final CleanerService cleanerService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final String secret;
    private final Long accessTokenExpiration;
    private final Long refreshTokenExpiration;
    private final Integer confirmationTimeInSeconds;

    public AccountServiceImpl(
            MailService mailService,
            DelayedJobService delayedJobService,
            AccountRepo accountRepo,
            RegistrationCodeRepo registrationCodeRepo,
            CleanerService cleanerService,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            @Value("${app.tokens.secret}") String secret,
            @Value("${app.tokens.access.expiration}") Long accessTokenExpiration,
            @Value("${app.tokens.refresh.expiration}") Long refreshTokenExpiration,
            @Value("${app.registration.confirmationtime.registration}") Integer confirmationTimeInSeconds
    ) {
        this.mailService = mailService;
        this.delayedJobService = delayedJobService;
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
    public void signup(Localizer localizer, SignupRequest signupRequest) throws FieldsException {
        checkSignup(localizer, signupRequest);

        String code = CodeGenerator.generate();
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
                        code
                )
        );

        mailService.send(
                signupRequest.getEmail(),
                localizer.getMessage(Message.CONFIRMATION_OF_REGISTRATION),
                localizer.getMessage(Message.CODE_FOR_CONFIRMATION_OF_REGISTRATION, code)
        );

        delayedJobService.schedule(
                () -> cleanerService.deleteNonActivatedUser(account.getUsername()),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void confirmRegistrationCode(Localizer localizer, ConfirmCodeRequest confirmCodeRequest) throws DescriptionException {
        if (confirmCodeRequest.getCode().length() != 4) {
            throw new DescriptionException(localizer.getMessage(Message.CODE_MUST_CONTAINS_4_SYMBOLS));
        }
        if (!registrationCodeRepo.existsByUsername(confirmCodeRequest.getUsername())) {
            throw new DescriptionException(localizer.getMessage(Message.CODE_EXPIRED));
        }
        registrationCodeRepo.deleteById(confirmCodeRequest.getUsername());
    }

    @Override
    public TokensResponse login(Localizer localizer, LoginRequest loginRequest) throws FieldsException, DescriptionException {
        checkLogin(localizer, loginRequest);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception ex) {
            throw new DescriptionException(localizer.getMessage(Message.AUTHORIZATION_FAILED));
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
    public TokensResponse refreshToken(String refreshToken) throws RefreshTokenFailedException {
        if (refreshToken == null) {
            throw new RefreshTokenFailedException();
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(refreshToken);
            String username = decodedJWT.getSubject();

            Optional<AccountEntity> account = accountRepo.findByUsername(username);
            if (account.isEmpty()) {
                throw new RefreshTokenFailedException();
            }
            AccountEntity accountEntity = account.get();

            String accessToken = JWT.create()
                    .withSubject(accountEntity.getUsername())
                    .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                    .sign(algorithm);

            return new TokensResponse(accessToken, refreshToken, username);
        } catch (Exception ex) {
            throw new RefreshTokenFailedException();
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String getUsername(String accessToken) throws AccountIsNotAuthorizedException {
        if (accessToken == null) {
            throw new AccountIsNotAuthorizedException();
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(accessToken);
            return decodedJWT.getSubject();
        } catch (Exception ex) {
            throw new AccountIsNotAuthorizedException();
        }
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public String getUsernameOrNull(String accessToken) {
        try {
            return getUsername(accessToken);
        } catch (AccountIsNotAuthorizedException ex) {
            return null;
        }
    }

    private void checkSignup(Localizer localizer, SignupRequest signupRequest) throws FieldsException {
        Map<String, String> fieldsErrors = new HashMap<>();

        if (signupRequest.getUsername().isEmpty()) {
            fieldsErrors.put(
                    FieldsException.USERNAME,
                    localizer.getMessage(Message.USERNAME_MUST_NOT_BE_EMPTY)
            );
        }
        if (signupRequest.getUsername().length() > 64) {
            fieldsErrors.put(
                    FieldsException.USERNAME,
                    localizer.getMessage(Message.USERNAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS)
            );
        }
        if (signupRequest.getPassword().length() < 8) {
            fieldsErrors.put(
                    FieldsException.PASSWORD,
                    localizer.getMessage(Message.PASSWORD_MUST_CONTAINS_MORE_THAN_8_SYMBOLS)
            );
        }
        if (signupRequest.getPassword().length() > 64) {
            fieldsErrors.put(
                    FieldsException.PASSWORD,
                    localizer.getMessage(Message.PASSWORD_MUST_CONTAINS_LESS_THAN_64_SYMBOLS)
            );
        }
        if (!signupRequest.getPassword().equals(signupRequest.getRepeatPassword())) {
            fieldsErrors.put(
                    FieldsException.REPEAT_PASSWORD,
                    localizer.getMessage(Message.PASSWORDS_DO_NOT_MATCH)
            );
        }
        if (signupRequest.getFirstName().isEmpty()) {
            fieldsErrors.put(
                    FieldsException.FIRST_NAME,
                    localizer.getMessage(Message.FIRST_NAME_MUST_NOT_BE_EMPTY)
            );
        }
        if (signupRequest.getFirstName().length() > 64) {
            fieldsErrors.put(
                    FieldsException.FIRST_NAME,
                    localizer.getMessage(Message.FIRST_NAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS)
            );
        }
        if (signupRequest.getLastName().isEmpty()) {
            fieldsErrors.put(
                    FieldsException.LAST_NAME,
                    localizer.getMessage(Message.LAST_NAME_MUST_NOT_BE_EMPTY)
            );
        }
        if (signupRequest.getLastName().length() > 64) {
            fieldsErrors.put(
                    FieldsException.LAST_NAME,
                    localizer.getMessage(Message.LAST_NAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS)
            );
        }
        if (!EmailChecker.emailMatches(signupRequest.getEmail())) {
            fieldsErrors.put(
                    FieldsException.EMAIL,
                    localizer.getMessage(Message.WRONG_EMAIL)
            );
        }
        if (!fieldsErrors.isEmpty()) {
            throw new FieldsException(fieldsErrors);
        }

        if (accountRepo.existsByUsername(signupRequest.getUsername())) {
            if (registrationCodeRepo.existsById(signupRequest.getUsername())) {
                fieldsErrors.put(
                        FieldsException.USERNAME,
                        localizer.getMessage(
                                Message.USERNAME_RESERVED_START,
                                signupRequest.getUsername(),
                                Message.USERNAME_RESERVED_END
                        )
                );
            } else {
                fieldsErrors.put(
                        FieldsException.USERNAME,
                        localizer.getMessage(
                                Message.USER_WITH_USERNAME_ALREADY_EXISTS_START,
                                signupRequest.getUsername(),
                                Message.USER_WITH_USERNAME_ALREADY_EXISTS_END
                        )
                );
            }
        }
        if (accountRepo.existsByEmail(signupRequest.getEmail())) {
            fieldsErrors.put(
                    FieldsException.EMAIL,
                    localizer.getMessage(
                            Message.USER_WITH_EMAIL_ALREADY_EXISTS_START,
                            signupRequest.getUsername(),
                            Message.USER_WITH_EMAIL_ALREADY_EXISTS_END
                    )
            );
        }
        if (!fieldsErrors.isEmpty()) {
            throw new FieldsException(fieldsErrors);
        }
    }

    private void checkLogin(Localizer localizer, LoginRequest loginRequest) throws FieldsException {
        Map<String, String> fieldsErrors = new HashMap<>();

        if (loginRequest.getUsername().isEmpty()) {
            fieldsErrors.put(
                    FieldsException.USERNAME,
                    localizer.getMessage(Message.USERNAME_MUST_NOT_BE_EMPTY)
            );
        }
        if (loginRequest.getUsername().length() > 64) {
            fieldsErrors.put(
                    FieldsException.USERNAME,
                    localizer.getMessage(Message.USERNAME_MUST_CONTAINS_LESS_THAN_64_SYMBOLS)
            );
        }
        if (loginRequest.getPassword().length() < 8) {
            fieldsErrors.put(
                    FieldsException.PASSWORD,
                    localizer.getMessage(Message.PASSWORD_MUST_CONTAINS_MORE_THAN_8_SYMBOLS)
            );
        }
        if (loginRequest.getPassword().length() > 64) {
            fieldsErrors.put(
                    FieldsException.PASSWORD,
                    localizer.getMessage(Message.PASSWORD_MUST_CONTAINS_LESS_THAN_64_SYMBOLS)
            );
        }
        if (!fieldsErrors.isEmpty()) {
            throw new FieldsException(fieldsErrors);
        }

        Optional<AccountEntity> account = accountRepo.findByUsername(loginRequest.getUsername());
        if (account.isEmpty()) {
            fieldsErrors.put(
                    FieldsException.USERNAME,
                    localizer.getMessage(
                            Message.USER_WITH_USERNAME_DOES_NOT_EXIST_START,
                            loginRequest.getUsername(),
                            Message.USER_WITH_USERNAME_DOES_NOT_EXIST_END
                    )
            );
            throw new FieldsException(fieldsErrors);
        }
        AccountEntity accountEntity = account.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), accountEntity.getPassword())) {
            fieldsErrors.put(
                    FieldsException.PASSWORD,
                    localizer.getMessage(Message.WRONG_PASSWORD)
            );
            throw new FieldsException(fieldsErrors);
        }
    }
}