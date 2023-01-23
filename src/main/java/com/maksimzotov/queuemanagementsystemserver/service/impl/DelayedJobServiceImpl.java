package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.service.DelayedJobService;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DelayedJobServiceImpl implements DelayedJobService {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @Override
    public void schedule(Runnable command, long delay, TimeUnit unit) {
        scheduledExecutorService.schedule(command, delay, unit);
    }
}
