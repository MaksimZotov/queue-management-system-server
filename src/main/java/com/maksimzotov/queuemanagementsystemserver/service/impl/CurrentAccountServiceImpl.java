package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.service.CurrentAccountService;
import com.maksimzotov.queuemanagementsystemserver.util.HandleRequestFromCurrentAccountNoReturnSAM;
import com.maksimzotov.queuemanagementsystemserver.util.HandleRequestFromCurrentAccountSAM;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Transactional
public class CurrentAccountServiceImpl implements CurrentAccountService {

    private final String secret;

    public CurrentAccountServiceImpl(
            @Value("${app.tokens.secret}") String secret
    ) {
        this.secret = secret;
    }

    @Override
    public  <T> T handleRequestFromCurrentAccount(
            HttpServletRequest request,
            HandleRequestFromCurrentAccountSAM<T> handleRequestFromCurrentAccountSAM
    ) throws Exception {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String refresh_token = authorizationHeader.substring("Bearer ".length());
            Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(refresh_token);
            String username = decodedJWT.getSubject();
            return handleRequestFromCurrentAccountSAM.handleRequestFromCurrentAccount(username);
        } else {
            throw new AccountIsNotAuthorizedException();
        }
    }

    @Override
    public void handleRequestFromCurrentAccountNoReturn(
            HttpServletRequest request,
            HandleRequestFromCurrentAccountNoReturnSAM handleRequestFromCurrentAccountNoReturnSAM
    ) throws Exception {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String refresh_token = authorizationHeader.substring("Bearer ".length());
            Algorithm algorithm = Algorithm.HMAC256(secret.getBytes());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(refresh_token);
            String username = decodedJWT.getSubject();
            handleRequestFromCurrentAccountNoReturnSAM.handleRequestFromCurrentAccount(username);
        } else {
            throw new AccountIsNotAuthorizedException();
        }
    }
}
