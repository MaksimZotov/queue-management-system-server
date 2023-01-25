package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.services.*;
import com.maksimzotov.queuemanagementsystemserver.model.type.QueueTypeModel;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface ServiceService {
    ContainerForList<QueueTypeModel> getServicesInLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException;
    QueueTypeModel createServiceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServiceRequest createQueueTypeRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteServiceInLocation(Localizer localizer, String accessToken, Long locationId, Long serviceId) throws DescriptionException, AccountIsNotAuthorizedException;
    ContainerForList<ServiceModel> getServicesInQueue(Localizer localizer, Long queueId) throws DescriptionException;
    ServicesSequenceModel getServicesSequencesInLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException;
    QueueTypeModel createServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServicesSequenceRequest createServicesSequenceRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, Long servicesSequenceId) throws DescriptionException, AccountIsNotAuthorizedException;
}
