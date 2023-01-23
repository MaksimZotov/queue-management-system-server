package com.maksimzotov.queuemanagementsystemserver.service;

import java.util.concurrent.TimeUnit;

public interface DelayedJobService {
    void schedule(Runnable command, long delay, TimeUnit unit);
}
