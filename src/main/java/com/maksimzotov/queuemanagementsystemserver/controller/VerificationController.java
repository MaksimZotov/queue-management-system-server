package com.maksimzotov.queuemanagementsystemserver.controller;

import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenIsMissingException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ErrorResult;
import com.maksimzotov.queuemanagementsystemserver.model.verification.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
@Slf4j
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        try {
            return ResponseEntity.ok().body(verificationService.signup(signupRequest));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Signup failed"));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody ConfirmCodeRequest confirmCodeRequest) {
        try {
            return ResponseEntity.ok().body(verificationService.confirmRegistrationCode(confirmCodeRequest));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Code confirmation failed"));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            return ResponseEntity.ok().body(verificationService.login(loginRequest));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Login failed"));
        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestParam(name = "refresh_token") String refreshToken) {
        try {
            return ResponseEntity.ok().body(verificationService.refreshToken(refreshToken));
        } catch (RefreshTokenIsMissingException ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Refresh token is missing"));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResult("Token refreshing failed"));
        }
    }
}
