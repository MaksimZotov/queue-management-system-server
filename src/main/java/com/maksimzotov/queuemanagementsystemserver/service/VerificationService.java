package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.entity.AccountEntity;
import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenIsMissingException;
import com.maksimzotov.queuemanagementsystemserver.model.verification.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.TokensResponse;

public interface VerificationService {
    AccountEntity signup(SignupRequest signupRequest);
    AccountEntity confirmRegistrationCode(ConfirmCodeRequest confirmCodeRequest);
    TokensResponse login(LoginRequest loginRequest);
    TokensResponse refreshToken(String refreshToken) throws RefreshTokenIsMissingException;
}
