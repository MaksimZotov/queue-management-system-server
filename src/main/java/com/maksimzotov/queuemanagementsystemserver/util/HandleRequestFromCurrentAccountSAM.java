package com.maksimzotov.queuemanagementsystemserver.util;

public interface HandleRequestFromCurrentAccountSAM<T> {
    T handleRequestFromCurrentAccount(String username);
}
