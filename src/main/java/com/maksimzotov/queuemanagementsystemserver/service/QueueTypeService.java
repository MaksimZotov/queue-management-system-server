package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.services.ServiceModel;
import com.maksimzotov.queuemanagementsystemserver.model.services.SetServicesInQueueTypeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.type.CreateQueueTypeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.type.QueueTypeModel;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface QueueTypeService {
    ContainerForList<QueueTypeModel> getQueueTypesInLocation(Localizer localizer, Long locationId) throws DescriptionException;
    QueueTypeModel createQueueTypesInLocation(Localizer localizer, String accessToken, Long locationId, CreateQueueTypeRequest createQueueTypeRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteQueueTypesInLocation(Localizer localizer, String accessToken, Long locationId, Long queueTypeId) throws DescriptionException, AccountIsNotAuthorizedException;

}
