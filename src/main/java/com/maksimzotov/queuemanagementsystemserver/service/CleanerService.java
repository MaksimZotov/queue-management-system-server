package com.maksimzotov.queuemanagementsystemserver.service;

public interface CleanerService {
    void deleteNonConfirmedUser(String username);
    void deleteNonConfirmedClient(Long clientId, String email);
}
