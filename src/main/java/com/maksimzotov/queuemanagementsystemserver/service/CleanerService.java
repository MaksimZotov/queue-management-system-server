package com.maksimzotov.queuemanagementsystemserver.service;

public interface CleanerService {
    void deleteNonActivatedUser(String username);
}
