package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.maksimzotov.queuemanagementsystemserver.QueueManagementSystemServerApplication;
import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.entity.RegistrationCodeEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenIsMissingException;
import com.maksimzotov.queuemanagementsystemserver.model.verification.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.repository.AccountRepo;
import com.maksimzotov.queuemanagementsystemserver.repository.RegistrationCodeRepo;
import com.maksimzotov.queuemanagementsystemserver.service.CleanerService;
import com.maksimzotov.queuemanagementsystemserver.service.VerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private final AccountRepo accountRepo;
    private final RegistrationCodeRepo registrationCodeRepo;
    private final CleanerService cleanerService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    private final String emailUsernameSender;
    private final String secret;
    private final Long accessTokenExpiration;
    private final Long refreshTokenExpiration;
    private final Integer confirmationTimeInSeconds;

    public VerificationServiceImpl(
            AccountRepo accountRepo,
            RegistrationCodeRepo registrationCodeRepo,
            CleanerService cleanerService, AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String emailUsernameSender,
            @Value("${app.tokens.secret}") String secret,
            @Value("${app.tokens.access.expiration}") Long accessTokenExpiration,
            @Value("${app.tokens.refresh.expiration}") Long refreshTokenExpiration,
            @Value("${app.registration.confirmationtime.registration}") Integer confirmationTimeInSeconds
    ) {
        this.accountRepo = accountRepo;
        this.registrationCodeRepo = registrationCodeRepo;
        this.cleanerService = cleanerService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.emailUsernameSender = emailUsernameSender;
        this.secret = secret;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.confirmationTimeInSeconds = confirmationTimeInSeconds;
    }

    @Override
    public AccountEntity signup(SignupRequest signupRequest) {
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

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailUsernameSender);
        mailMessage.setTo(signupRequest.getEmail());
        mailMessage.setSubject("Registration code");
        mailMessage.setText("Registration code: " + code);
        mailSender.send(mailMessage);

        QueueManagementSystemServerApplication.scheduledExecutorService.schedule(() ->
                cleanerService.deleteNonActivatedUser(
                        account.getUsername()
                ),
                confirmationTimeInSeconds,
                TimeUnit.SECONDS
        );

        return account;
    }

    @Override
    public AccountEntity confirmRegistrationCode(ConfirmCodeRequest confirmCodeRequest) {
        Optional<RegistrationCodeEntity> registrationCode = registrationCodeRepo.findById(confirmCodeRequest.getUsername());
        if (registrationCode.isEmpty()) {
            return null;
        }
        registrationCodeRepo.deleteById(confirmCodeRequest.getUsername());
        Optional<AccountEntity> account = accountRepo.findByUsername(confirmCodeRequest.getUsername());
        if (account.isEmpty()) {
            return null;
        }
        return account.get();
    }

    @Override
    public TokensResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        log.info("Username is: {}", username);
        log.info("Password is: {}", password);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                username,
                password
        );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);

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

        return new TokensResponse(access, refresh);
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

            return new TokensResponse(accessToken, refreshToken);
        } else {
            throw new RefreshTokenIsMissingException();
        }
    }
}
