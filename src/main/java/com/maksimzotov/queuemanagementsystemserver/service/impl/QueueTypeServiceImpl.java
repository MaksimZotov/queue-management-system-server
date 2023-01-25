package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.services.ServiceModel;
import com.maksimzotov.queuemanagementsystemserver.model.services.SetServicesInQueueTypeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.type.CreateQueueTypeRequest;
import com.maksimzotov.queuemanagementsystemserver.model.type.QueueTypeModel;
import com.maksimzotov.queuemanagementsystemserver.service.QueueTypeService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class QueueTypeServiceImpl implements QueueTypeService {
    @Override
    public ContainerForList<QueueTypeModel> getQueueTypesInLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException, AccountIsNotAuthorizedException {
        // TODO
        return null;
    }

    @Override
    public QueueTypeModel createQueueTypesInLocation(Localizer localizer, String accessToken, Long locationId, CreateQueueTypeRequest createQueueTypeRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        // TODO
        return null;
    }

    @Override
    public void deleteQueueTypesInLocation(Localizer localizer, String accessToken, Long locationId, Long queueTypeId) throws DescriptionException, AccountIsNotAuthorizedException {
        // TODO
    }
}
