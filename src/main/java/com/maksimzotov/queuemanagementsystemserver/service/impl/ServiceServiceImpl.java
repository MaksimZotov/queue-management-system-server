package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.services.*;
import com.maksimzotov.queuemanagementsystemserver.model.template.QueueClassModel;
import com.maksimzotov.queuemanagementsystemserver.service.ServiceService;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class ServiceServiceImpl implements ServiceService {
    @Override
    public ContainerForList<QueueClassModel> getServicesInLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException {
        // TODO
        return null;
    }

    @Override
    public QueueClassModel createServiceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServiceRequest createQueueClassRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        // TODO
        return null;
    }

    @Override
    public void deleteServiceInLocation(Localizer localizer, String accessToken, Long locationId, Long serviceId) throws DescriptionException, AccountIsNotAuthorizedException {
        // TODO
    }

    @Override
    public ContainerForList<ServiceModel> getServicesInQueue(Localizer localizer, Long queueId) throws DescriptionException {
        // TODO
        return null;
    }

    @Override
    public ContainerForList<ServiceModel> setServicesInQueue(Localizer localizer, String accessToken, Long queueId, SetServicesInQueueRequest setServicesInQueueRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        // TODO
        return null;
    }

    @Override
    public ServicesSequenceModel getServicesSequencesInLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException {
        // TODO
        return null;
    }

    @Override
    public QueueClassModel createServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServicesSequenceRequest createServicesSequenceRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        // TODO
        return null;
    }

    @Override
    public void deleteServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, Long servicesSequenceId) throws DescriptionException, AccountIsNotAuthorizedException {
        // TODO
    }
}
