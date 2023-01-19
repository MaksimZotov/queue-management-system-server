package com.maksimzotov.queuemanagementsystemserver.controller;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenIsMissingException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.verification.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.service.AccountService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final MessageSource messageSource;

    private final AccountService accountService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            HttpServletRequest request,
            @RequestBody SignupRequest signupRequest
    ) {
        try {
            accountService.signup(getLocalizer(request), signupRequest);
            return ResponseEntity.ok().build();
        } catch (FieldsException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getErrors()));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(
            HttpServletRequest request,
            @RequestBody ConfirmCodeRequest confirmCodeRequest
    ) {
        try {
            accountService.confirmRegistrationCode(getLocalizer(request), confirmCodeRequest);
            return ResponseEntity.ok().build();
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(
            HttpServletRequest request,
            @RequestBody LoginRequest loginRequest
    ) {
        try {
            return ResponseEntity.ok().body(accountService.login(getLocalizer(request), loginRequest));
        } catch (FieldsException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getErrors()));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            @RequestParam(name = "refresh_token")
            String refreshToken
    ) {
        try {
            return ResponseEntity.ok().body(accountService.refreshToken(refreshToken));
        } catch (RefreshTokenIsMissingException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(getLocalizer(request).getMessage(Message.REFRESH_TOKEN_IS_MISSING)));
        }  catch (TokenExpiredException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Localizer getLocalizer(HttpServletRequest request) {
        return new Localizer(request.getLocale(), messageSource);
    }
}
