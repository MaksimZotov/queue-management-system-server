package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenIsMissingException;
import com.maksimzotov.queuemanagementsystemserver.model.verification.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.TokensResponse;

public interface VerificationService {
    void signup(SignupRequest signupRequest) throws FieldsException;
    void confirmRegistrationCode(ConfirmCodeRequest confirmCodeRequest) throws FieldsException, DescriptionException;
    TokensResponse login(LoginRequest loginRequest) throws FieldsException, DescriptionException;
    TokensResponse refreshToken(String refreshToken) throws RefreshTokenIsMissingException;
}
