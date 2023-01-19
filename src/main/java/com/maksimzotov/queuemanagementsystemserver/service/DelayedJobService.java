package com.maksimzotov.queuemanagementsystemserver.service;

import java.util.concurrent.TimeUnit;

public interface DelayedJobService {
    public void schedule(Runnable command, long delay, TimeUnit unit);
}
