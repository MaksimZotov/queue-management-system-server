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
import com.maksimzotov.queuemanagementsystemserver.model.account.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RegistrationCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.service.CleanerService;
import com.maksimzotov.queuemanagementsystemserver.service.JobService;
import com.maksimzotov.queuemanagementsystemserver.service.MailService;
import com.maksimzotov.queuemanagementsystemserver.util.CodeGenerator;
import com.maksimzotov.queuemanagementsystemserver.util.EmailChecker;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final MailService mailService;
    private final JobService jobService;
    private final AccountRepo accountRepo;
    private final RegistrationCodeRepo registrationCodeRepo;
    private final CleanerService cleanerService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.tokens.secret}")
    private String secret;
    @Value("${app.tokens.expiration.access}")
    private Long accessTokenExpiration;
    @Value("${app.tokens.expiration.refresh}")
    private Long refreshTokenExpiration;
    @Value("${app.confirmation.time.registration}")
    private Integer confirmationTimeInSeconds;

    @Override
    public void signup(Localizer localizer, SignupRequest signupRequest) throws FieldsException {
        checkSignup(localizer, signupRequest);

        Integer code = CodeGenerator.generateCodeForEmail();
        AccountEntity account = new AccountEntity(
                null,
                signupRequest.getEmail(),
                signupRequest.getFirstName(),
                signupRequest.getLastName(),
                passwordEncoder.encode(signupRequest.getPassword())
        );
        accountRepo.save(account);
        registrationCodeRepo.save(
                new RegistrationCodeEntity(
                        account.getEmail(),
                        code
                )
        );

        mailService.send(
                signupRequest.getEmail(),
                localizer.getMessage(Message.CONFIRMATION_OF_REGISTRATION),
                localizer.getMessage(Message.CODE_FOR_CONFIRMATION_OF_REGISTRATION, code)
        );

        jobService.schedule(
                () -> cleanerService.deleteNonConfirmedUser(account.getEmail()),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );
    }

    @Override
    public void confirmRegistrationCode(Localizer localizer, ConfirmCodeRequest confirmCodeRequest) throws DescriptionException {
        if (confirmCodeRequest.getCode().length() != 4) {
            throw new DescriptionException(localizer.getMessage(Message.CODE_MUST_CONTAINS_4_SYMBOLS));
        }
        if (!registrationCodeRepo.existsByEmail(confirmCodeRequest.getEmail())) {
            throw new DescriptionException(localizer.getMessage(Message.CODE_EXPIRED));
        }
        registrationCodeRepo.deleteByEmail(confirmCodeRequest.getEmail());
    }

    @Override
    public TokensResponse login(Localizer localizer, LoginRequest loginRequest) throws FieldsException, DescriptionException {
        checkLogin(localizer, loginRequest);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
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

        Optional<AccountEntity> account = accountRepo.findByEmail(user.getUsername());
        if (account.isEmpty()) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.ACCOUNT_WITH_EMAIL_DOES_NOT_EXIST_START,
                            user.getUsername(),
                            Message.ACCOUNT_WITH_EMAIL_DOES_NOT_EXIST_END
                    )
            );
        }
        AccountEntity accountEntity = account.get();

        return new TokensResponse(access, refresh, accountEntity.getId());
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
            String email = decodedJWT.getSubject();

            Optional<AccountEntity> account = accountRepo.findByEmail(email);
            if (account.isEmpty()) {
                throw new RefreshTokenFailedException();
            }
            AccountEntity accountEntity = account.get();

            String accessToken = JWT.create()
                    .withSubject(accountEntity.getEmail())
                    .withExpiresAt(new Date(System.currentTimeMillis() + accessTokenExpiration))
                    .sign(algorithm);

            String newRefreshToken = JWT.create()
                    .withSubject(accountEntity.getEmail())
                    .withExpiresAt(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                    .sign(algorithm);

            return new TokensResponse(accessToken, newRefreshToken, accountEntity.getId());
        } catch (Exception ex) {
            throw new RefreshTokenFailedException();
        }
    }

    @Override
    public String getEmail(String accessToken) throws AccountIsNotAuthorizedException {
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
    public String getEmailOrNull(String accessToken) {
        try {
            return getEmail(accessToken);
        } catch (AccountIsNotAuthorizedException ex) {
            return null;
        }
    }

    private void checkSignup(Localizer localizer, SignupRequest signupRequest) throws FieldsException {
        Map<String, String> fieldsErrors = new HashMap<>();

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

        if (accountRepo.existsByEmail(signupRequest.getEmail())) {
            if (registrationCodeRepo.existsByEmail(signupRequest.getEmail())) {
                fieldsErrors.put(
                        FieldsException.EMAIL,
                        localizer.getMessage(
                                Message.USER_WITH_EMAIL_RESERVED_START,
                                signupRequest.getEmail(),
                                Message.USER_WITH_EMAIL_RESERVED_END
                        )
                );
            } else {
                fieldsErrors.put(
                        FieldsException.EMAIL,
                        localizer.getMessage(
                                Message.USER_WITH_EMAIL_ALREADY_EXISTS_START,
                                signupRequest.getEmail(),
                                Message.USER_WITH_EMAIL_ALREADY_EXISTS_END
                        )
                );
            }
        }
        if (!fieldsErrors.isEmpty()) {
            throw new FieldsException(fieldsErrors);
        }
    }

    private void checkLogin(Localizer localizer, LoginRequest loginRequest) throws FieldsException {
        Map<String, String> fieldsErrors = new HashMap<>();

        if (!EmailChecker.emailMatches(loginRequest.getEmail())) {
            fieldsErrors.put(
                    FieldsException.EMAIL,
                    localizer.getMessage(Message.WRONG_EMAIL)
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

        Optional<AccountEntity> account = accountRepo.findByEmail(loginRequest.getEmail());
        if (account.isEmpty()) {
            fieldsErrors.put(
                    FieldsException.EMAIL,
                    localizer.getMessage(
                            Message.USER_WITH_EMAIL_DOES_NOT_EXIST_START,
                            loginRequest.getEmail(),
                            Message.USER_WITH_EMAIL_DOES_NOT_EXIST_END
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
