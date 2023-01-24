package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.services.*;
import com.maksimzotov.queuemanagementsystemserver.model.template.QueueClassModel;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface ServiceService {
    ContainerForList<QueueClassModel> getServicesInLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException;
    QueueClassModel createServiceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServiceRequest createQueueClassRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteServiceInLocation(Localizer localizer, String accessToken, Long locationId, Long serviceId) throws DescriptionException, AccountIsNotAuthorizedException;
    ContainerForList<ServiceModel> addServicesToQueue(Localizer localizer, String accessToken, Long queueId, AddOrRemoveServicesRequest addOrRemoveServicesRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void removeServicesFromQueue(Localizer localizer, String accessToken, Long queueId, AddOrRemoveServicesRequest addOrRemoveServicesRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    ServicesSequenceModel getServicesSequencesInLocation(Localizer localizer, String accessToken, Long locationId) throws DescriptionException;
    QueueClassModel createServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServicesSequenceRequest createServicesSequenceRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, Long servicesSequenceId) throws DescriptionException, AccountIsNotAuthorizedException;
}
