package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.queue.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface QueueService {
    QueueModel createQueue(Localizer localizer, String accessToken, Long locationId, CreateQueueRequest createQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException;
    ContainerForList<QueueModel> getQueues(Localizer localizer, String accessToken, Long locationId);
    QueueStateModel getQueueState(Localizer localizer, String accessToken, Long queueId) throws DescriptionException;
}
