package com.maksimzotov.queuemanagementsystemserver.service;

public interface CleanerService {
    void deleteNonActivatedUser(String username);
    void deleteJoinClientCode(Long queueId, String email);
    void deleteRejoinClientCode(Long queueId, String email);
}
