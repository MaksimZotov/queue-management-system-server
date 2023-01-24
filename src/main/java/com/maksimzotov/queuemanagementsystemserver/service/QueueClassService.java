package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.template.CreateQueueClassRequest;
import com.maksimzotov.queuemanagementsystemserver.model.template.QueueClassModel;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface QueueClassService {
    ContainerForList<QueueClassModel> getQueueClassesInLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException, AccountIsNotAuthorizedException;
    QueueClassModel createQueueClassesInLocation(Localizer localizer, String accessToken, Long locationId, CreateQueueClassRequest createQueueClassRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteQueueClassesInLocation(Localizer localizer, String accessToken, Long locationId, Long queueClassId) throws DescriptionException, AccountIsNotAuthorizedException;
}
