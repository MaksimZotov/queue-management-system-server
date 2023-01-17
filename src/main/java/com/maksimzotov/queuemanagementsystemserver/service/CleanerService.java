package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;

public interface CleanerService {
    void deleteNonActivatedUser(String username);
    void deleteJoinClientCode(Long queueId, String email) throws DescriptionException;
    void deleteRejoinClientCode(Long queueId, String email);
}
