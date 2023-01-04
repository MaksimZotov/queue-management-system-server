package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.util.HandleRequestFromCurrentAccountNoReturnSAM;
import com.maksimzotov.queuemanagementsystemserver.util.HandleRequestFromCurrentAccountSAM;

import javax.servlet.http.HttpServletRequest;

public interface CurrentAccountService {
    <T> T handleRequestFromCurrentAccount(
            HttpServletRequest request,
            HandleRequestFromCurrentAccountSAM<T> handleRequestFromCurrentAccountSAM
    ) throws Exception;

    void handleRequestFromCurrentAccountNoReturn(
            HttpServletRequest request,
            HandleRequestFromCurrentAccountNoReturnSAM handleRequestFromCurrentAccountNoReturnSAM
    ) throws Exception;
}
