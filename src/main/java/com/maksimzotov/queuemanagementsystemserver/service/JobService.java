package com.maksimzotov.queuemanagementsystemserver.service;

import java.util.concurrent.TimeUnit;

public interface JobService {
    void schedule(Runnable command, long delay, TimeUnit unit);
    void runAsync(Runnable command);
}
