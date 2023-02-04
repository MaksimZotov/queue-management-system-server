package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.FieldsException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.RefreshTokenFailedException;
import com.maksimzotov.queuemanagementsystemserver.model.account.ConfirmCodeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.LoginRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.SignupRequest;
import com.maksimzotov.queuemanagementsystemserver.model.account.TokensResponse;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface AccountService {
    void signup(Localizer localizer, SignupRequest signupRequest) throws FieldsException;
    void confirmRegistrationCode(Localizer localizer, ConfirmCodeRequest confirmCodeRequest) throws DescriptionException;
    TokensResponse login(Localizer localizer, LoginRequest loginRequest) throws FieldsException, DescriptionException;
    TokensResponse refreshToken(String refreshToken) throws RefreshTokenFailedException;
    String getEmail(String accessToken) throws AccountIsNotAuthorizedException;
    String getEmailOrNull(String accessToken);
}
