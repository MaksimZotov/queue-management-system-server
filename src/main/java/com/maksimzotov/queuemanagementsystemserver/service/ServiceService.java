package com.maksimzotov.queuemanagementsystemserver.service;

import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.model.base.ContainerForList;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.CreateServicesSequenceRequest;
import com.maksimzotov.queuemanagementsystemserver.model.sequence.ServicesSequenceModel;
import com.maksimzotov.queuemanagementsystemserver.model.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;

public interface ServiceService {
    ContainerForList<ServiceModel> getServicesInLocation(Localizer localizer, Long locationId) throws DescriptionException;
    ServiceModel createServiceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServiceRequest createSpecialistRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteServiceInLocation(Localizer localizer, String accessToken, Long locationId, Long serviceId) throws DescriptionException, AccountIsNotAuthorizedException;
    ContainerForList<ServiceModel> getServicesInQueue(Localizer localizer, Long queueId) throws DescriptionException;
    ContainerForList<ServiceModel> getServicesInSpecialist(Localizer localizer, Long queueTypeId) throws DescriptionException;
    ContainerForList<ServicesSequenceModel> getServicesSequencesInLocation(Localizer localizer, Long locationId) throws DescriptionException;
    ServicesSequenceModel createServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, CreateServicesSequenceRequest createServicesSequenceRequest) throws DescriptionException, AccountIsNotAuthorizedException;
    void deleteServicesSequenceInLocation(Localizer localizer, String accessToken, Long locationId, Long servicesSequenceId) throws DescriptionException, AccountIsNotAuthorizedException;
}
