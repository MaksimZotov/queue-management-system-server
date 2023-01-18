package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenIsMissingException;
import com.maksimzotov.queuemanagementsystemserver.model.verification.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.model.verification.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

import javax.servlet.http.HttpServletRequest;

public interface VerificationService {
    void signup(Localizer localizer, SignupRequest signupRequest) throws FieldsException;
    void confirmRegistrationCode(Localizer localizer, ConfirmCodeRequest confirmCodeRequest) throws DescriptionException;
    TokensResponse login(Localizer localizer, LoginRequest loginRequest) throws FieldsException, DescriptionException;
    TokensResponse refreshToken(String refreshToken) throws RefreshTokenIsMissingException;
}
