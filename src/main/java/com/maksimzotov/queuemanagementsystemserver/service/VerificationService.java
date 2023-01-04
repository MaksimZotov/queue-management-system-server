package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenIsMissingException;
import com.maksimzotov.queuemanagementsystemserver.model.verification.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.TokensResponse;

public interface VerificationService {
    void signup(SignupRequest signupRequest);
    void confirmRegistrationCode(ConfirmCodeRequest confirmCodeRequest);
    TokensResponse login(LoginRequest loginRequest);
    TokensResponse refreshToken(String refreshToken) throws RefreshTokenIsMissingException;
}
