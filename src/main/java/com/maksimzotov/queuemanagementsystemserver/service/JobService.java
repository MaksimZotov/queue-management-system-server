package com.maksimzotov.queuemanagementsystemserver.service;

public interface JobService {
    void runAsync(Runnable command);
}
