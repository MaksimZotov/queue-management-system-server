package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.service.JobService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class JobServiceImpl implements JobService {

    @Value("${app.threadPoolSize}")
    private Integer threadPoolSize;

    private volatile ExecutorService executorService;

    @Override
    public void runAsync(Runnable command) {
        getExecutorService().submit(command);
    }

    private ExecutorService getExecutorService() {
        if (executorService == null) {
            synchronized (this) {
                executorService = Executors.newFixedThreadPool(threadPoolSize);
            }
        }
        return executorService;
    }
}
