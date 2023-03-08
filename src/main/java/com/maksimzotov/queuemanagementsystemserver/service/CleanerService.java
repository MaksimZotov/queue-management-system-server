package com.maksimzotov.queuemanagementsystemserver.service;

public interface CleanerService {
    void deleteNonConfirmedUser(String email);
    void deleteNonConfirmedClient(Long clientId, String email);
}
