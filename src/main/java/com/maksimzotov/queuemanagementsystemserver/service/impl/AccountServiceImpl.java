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

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final MailService mailService;
    private final AccountRepo accountRepo;
    private final RegistrationCodeRepo registrationCodeRepo;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    @Value("${app.tokens.secret}")
    private String secret;
    @Value("${app.tokens.expiration.access}")
    private Long accessTokenExpiration;
    @Value("${app.tokens.expiration.refresh}")
    private Long refreshTokenExpiration;
    @Value("${app.confirmation.time.registration}")
    private Integer registrationTime;

    @Override
    public void signup(Localizer localizer, SignupRequest signupRequest) throws FieldsException {
        AccountEntity accountEntity = checkSignup(localizer, signupRequest);
        if (accountEntity == null) {
            accountEntity = new AccountEntity();
        }
        accountEntity.setEmail(signupRequest.getEmail());
        accountEntity.setFirstName(signupRequest.getFirstName());
        accountEntity.setLastName(signupRequest.getLastName());
        accountEntity.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        accountEntity.setRegistrationTimestamp(new Date());

        Integer code = CodeGenerator.generateCodeForEmail();

        accountRepo.save(accountEntity);
        registrationCodeRepo.save(
                new RegistrationCodeEntity(
                        accountEntity.getEmail(),
                        code
                )
        );

        mailService.send(
                signupRequest.getEmail(),
                localizer.getMessage(Message.CONFIRMATION_OF_REGISTRATION),
                localizer.getMessage(Message.CODE_FOR_CONFIRMATION_OF_REGISTRATION, code)
        );
    }

    @Override
    public void confirmRegistrationCode(Localizer localizer, ConfirmCodeRequest confirmCodeRequest) throws DescriptionException {
        if (confirmCodeRequest.getCode().length() != 4) {
            throw new DescriptionException(localizer.getMessage(Message.CODE_MUST_CONTAINS_4_SYMBOLS));
        }

        Optional<AccountEntity> account = accountRepo.findByEmail(confirmCodeRequest.getEmail());
        if (account.isEmpty()) {
            throw new DescriptionException(
                    localizer.getMessage(
                            Message.ACCOUNT_WITH_EMAIL_DOES_NOT_EXIST_START,
                            confirmCodeRequest.getEmail(),
                            Message.ACCOUNT_WITH_EMAIL_DOES_NOT_EXIST_END
                    )
            );
        }
        AccountEntity accountEntity = account.get();

        Optional<RegistrationCodeEntity> registrationCode = registrationCodeRepo.findByEmail(confirmCodeRequest.getEmail());
        if (registrationCode.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CODE_EXPIRED));
        }
        RegistrationCodeEntity registrationCodeEntity = registrationCode.get();

        if (new Date().getTime() - accountEntity.getRegistrationTimestamp().getTime() > registrationTime) {
            throw new DescriptionException(localizer.getMessage(Message.CODE_EXPIRED));
        }

        if (!registrationCodeEntity.getCode().toString().equals(confirmCodeRequest.getCode())) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_CODE));
        }

        registrationCodeRepo.delete(registrationCodeEntity);
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

    private AccountEntity checkSignup(Localizer localizer, SignupRequest signupRequest) throws FieldsException {
        Map<String, String> fieldsErrors = new HashMap<>();

        if (!EmailChecker.emailMatches(signupRequest.getEmail())) {
            fieldsErrors.put(
                    FieldsException.EMAIL,
                    localizer.getMessage(Message.WRONG_EMAIL)
            );
        }
        if (signupRequest.getPassword().length() < 8) {
            fieldsErrors.put(
                    FieldsException.PASSWORD,
                    localizer.getMessage(Message.PASSWORD_MUST_CONTAINS_MORE_THAN_7_SYMBOLS)
            );
        }
        if (signupRequest.getPassword().length() > 64) {
            fieldsErrors.put(
                    FieldsException.PASSWORD,
                    localizer.getMessage(Message.PASSWORD_MUST_CONTAINS_LESS_THAN_65_SYMBOLS)
            );
        }
        if (signupRequest.getPassword().contains(" ")) {
            fieldsErrors.put(
                    FieldsException.PASSWORD,
                    localizer.getMessage(Message.PASSWORD_MUST_NOT_CONTAINS_WHITESPACES)
            );
        }
        if (!signupRequest.getPassword().equals(signupRequest.getRepeatPassword())) {
            fieldsErrors.put(
                    FieldsException.REPEAT_PASSWORD,
                    localizer.getMessage(Message.PASSWORDS_DO_NOT_MATCH)
            );
        }
        if (signupRequest.getFirstName().isBlank()) {
            fieldsErrors.put(
                    FieldsException.FIRST_NAME,
                    localizer.getMessage(Message.FIRST_NAME_MUST_NOT_BE_EMPTY)
            );
        }
        if (signupRequest.getLastName().isBlank()) {
            fieldsErrors.put(
                    FieldsException.LAST_NAME,
                    localizer.getMessage(Message.LAST_NAME_MUST_NOT_BE_EMPTY)
            );
        }
        if (!fieldsErrors.isEmpty()) {
            throw new FieldsException(fieldsErrors);
        }

        Optional<AccountEntity> account = accountRepo.findByEmail(signupRequest.getEmail());
        if (account.isPresent()) {
            if (registrationCodeRepo.existsByEmail(signupRequest.getEmail())) {
                AccountEntity accountEntity = account.get();
                if (new Date().getTime() - accountEntity.getRegistrationTimestamp().getTime() < registrationTime) {
                    fieldsErrors.put(
                            FieldsException.EMAIL,
                            localizer.getMessage(
                                    Message.USER_WITH_EMAIL_RESERVED_START,
                                    signupRequest.getEmail(),
                                    Message.USER_WITH_EMAIL_RESERVED_END
                            )
                    );
                } else {
                    return accountEntity;
                }
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
        return null;
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
                    localizer.getMessage(Message.PASSWORD_MUST_CONTAINS_MORE_THAN_7_SYMBOLS)
            );
        }
        if (loginRequest.getPassword().length() > 64) {
            fieldsErrors.put(
                    FieldsException.PASSWORD,
                    localizer.getMessage(Message.PASSWORD_MUST_CONTAINS_LESS_THAN_65_SYMBOLS)
            );
        }
        if (loginRequest.getPassword().contains(" ")) {
            fieldsErrors.put(
                    FieldsException.PASSWORD,
                    localizer.getMessage(Message.PASSWORD_MUST_NOT_CONTAINS_WHITESPACES)
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
        if (registrationCodeRepo.existsByEmail(loginRequest.getEmail())) {
            fieldsErrors.put(
                    FieldsException.EMAIL,
                    localizer.getMessage(
                            Message.USER_WITH_EMAIL_IS_NOT_CONFIRMED_START,
                            loginRequest.getEmail(),
                            Message.USER_WITH_EMAIL_IS_NOT_CONFIRMED_END
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
