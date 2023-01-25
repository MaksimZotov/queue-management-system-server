package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.type.CreateQueueTypeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.type.QueueTypeModel;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface QueueTypeService {
    ContainerForList<QueueTypeModel> getQueueTypesInLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException, AccountIsNotAuthorizedException;
    QueueTypeModel createQueueTypesInLocation(Localizer localizer, String accessToken, Long locationId, CreateQueueTypeRequest createQueueTypeRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteQueueTypesInLocation(Localizer localizer, String accessToken, Long locationId, Long queueTypeId) throws DescriptionException, AccountIsNotAuthorizedException;
}
