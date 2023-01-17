package com.maksimzotov.queuemanagementsystemserver.util;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;

public interface HandleRequestFromCurrentAccountSAM<T> {
    T handleRequestFromCurrentAccount(String username) throws Exception;
}
