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
import com.maksimzotov.queuemanagementsystemserver.service.VerificationService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final MessageSource messageSource;

    private final VerificationService verificationService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            HttpServletRequest request,
            @RequestBody SignupRequest signupRequest
    ) {
        try {
            verificationService.signup(getLocalizer(request), signupRequest);
            return ResponseEntity.ok().build();
        } catch (FieldsException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getErrors()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(
            HttpServletRequest request,
            @RequestBody ConfirmCodeRequest confirmCodeRequest
    ) {
        try {
            verificationService.confirmRegistrationCode(getLocalizer(request), confirmCodeRequest);
            return ResponseEntity.ok().build();
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(
            HttpServletRequest request,
            @RequestBody LoginRequest loginRequest
    ) {
        try {
            return ResponseEntity.ok().body(verificationService.login(getLocalizer(request), loginRequest));
        } catch (FieldsException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getErrors()));
        } catch (DescriptionException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(ex.getDescription()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(
            HttpServletRequest request,
            @RequestParam(name = "refresh_token"
            ) String refreshToken) {
        try {
            return ResponseEntity.ok().body(verificationService.refreshToken(refreshToken));
        } catch (RefreshTokenIsMissingException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult(getLocalizer(request).getMessage(Message.REFRESH_TOKEN_IS_MISSING)));
        }  catch (TokenExpiredException ex) {
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private Localizer getLocalizer(HttpServletRequest request) {
        return new Localizer(request.getLocale(), messageSource);
    }
}
