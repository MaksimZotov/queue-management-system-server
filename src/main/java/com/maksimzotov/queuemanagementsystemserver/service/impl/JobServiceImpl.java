package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.service.JobService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class JobServiceImpl implements JobService {

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    public void schedule(Runnable command, long delay, TimeUnit unit) {
        scheduledExecutorService.schedule(command, delay, unit);
    }

    @Override
    public void runAsync(Runnable command) {
        executorService.submit(command);
    }
}
