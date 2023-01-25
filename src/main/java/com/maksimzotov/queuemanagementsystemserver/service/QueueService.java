package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface QueueService {
    Queue createQueue(Localizer localizer, String accessToken, Long locationId, CreateQueueRequest createQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException;
    ContainerForList<Queue> getQueues(Localizer localizer, String accessToken, Long locationId) throws DescriptionException;
    QueueState getQueueState(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException;
    QueueState getCurrentQueueState(Long queueId);
    QueueState updateCurrentQueueState(Long queueId);
    void changePausedState(Localizer localizer, String accessToken, Long queueId, Boolean paused) throws DescriptionException, AccountIsNotAuthorizedException;
    void changePausedStateInLocation(Localizer localizer, String accessToken, Long locationId, Boolean paused) throws DescriptionException, AccountIsNotAuthorizedException;
}
