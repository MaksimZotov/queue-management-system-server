package com.maksimzotov.queuemanagementsystemserver.service.impl;

import com.maksimzotov.queuemanagementsystemserver.entity.*;
import com.maksimzotov.queuemanagementsystemserver.exceptions.AccountIsNotAuthorizedException;
import com.maksimzotov.queuemanagementsystemserver.exceptions.DescriptionException;
import com.maksimzotov.queuemanagementsystemserver.message.Message;
import com.maksimzotov.queuemanagementsystemserver.model.client.*;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationChange;
import com.maksimzotov.queuemanagementsystemserver.model.location.LocationState;
import com.maksimzotov.queuemanagementsystemserver.repository.*;
import com.maksimzotov.queuemanagementsystemserver.service.*;
import com.maksimzotov.queuemanagementsystemserver.util.CodeGenerator;
import com.maksimzotov.queuemanagementsystemserver.util.Localizer;
import com.maksimzotov.queuemanagementsystemserver.util.PhoneHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final AccountService accountService;
    private final LocationService locationService;
    private final RightsService rightsService;
    private final SmsService smsService;
    private final ClientRepo clientRepo;
    private final QueueRepo queueRepo;
    private final LocationRepo locationRepo;
    private final ServiceRepo serviceRepo;
    private final ServicesSequenceRepo servicesSequenceRepo;
    private final ServiceInServicesSequenceRepo serviceInServicesSequenceRepo;
    private final ClientToChosenServiceRepo clientToChosenServiceRepo;
    @Value("${app.confirmation.time.join}")
    private Integer confirmationTime;
    @Value("${app.client.url}")
    private String clientBaseUrl;

    @Override
    public ClientModel createClient(Localizer localizer, String accessToken, Long locationId, CreateClientRequest createClientRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        Map<Long, Integer> serviceIdsToOrderNumbers = checkAddClientRequest(localizer, locationId, createClientRequest);
        return createClient(localizer, accessToken, locationId, createClientRequest, serviceIdsToOrderNumbers);
    }

    @Override
    public void changeClient(Localizer localizer, String accessToken, Long locationId, Long clientId, ChangeClientRequest changeClientRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
        if (changeClientRequest.getServiceIdsToOrderNumbers().isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.INCORRECT_REQUEST));
        }

        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientEntity clientEntity = client.get();
        clientEntity.setWaitTimestamp(new Date());
        clientRepo.save(clientEntity);

        clientToChosenServiceRepo.deleteByPrimaryKeyClientId(clientEntity.getId());

        Optional<QueueEntity> queue = queueRepo.findByClientId(clientEntity.getId());
        if (queue.isPresent()) {
            QueueEntity queueEntity = queue.get();
            queueEntity.setClientId(null);
            queueRepo.save(queueEntity);
        }

        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = new ArrayList<>();
        for (Map.Entry<Long, Integer> serviceIdToOrderNumber : changeClientRequest.getServiceIdsToOrderNumbers().entrySet()) {
            clientToChosenServiceEntities.add(
                    new ClientToChosenServiceEntity(
                            new ClientToChosenServiceEntity.PrimaryKey(
                                    clientEntity.getId(),
                                    serviceIdToOrderNumber.getKey()
                            ),
                            serviceIdToOrderNumber.getValue()
                    )
            );
        }
        clientToChosenServiceRepo.saveAll(clientToChosenServiceEntities);

        List<ServiceEntity> serviceEntities = serviceRepo.findAllByLocationIdAndAssignedToClient(locationId, clientId);

        locationService.updateLocationState(
                locationId,
                new LocationChange.UpdateClient(
                        LocationState.Client.toModel(
                                clientEntity,
                                serviceEntities,
                                clientToChosenServiceEntities,
                                null
                        )
                )
        );
    }

    @Override
    public void callClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }

        QueueEntity queueEntity = queue.get();
        if (queueEntity.getClientId() != null) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_ALREADY_ASSIGNED_TO_QUEUE));
        }

        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientEntity clientEntity = client.get();

        queueEntity.setClientId(clientId);
        queueRepo.save(queueEntity);

        List<ServiceEntity> serviceEntities = serviceRepo.findAllByLocationIdAndAssignedToClient(queueEntity.getLocationId(), clientId);
        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(clientId);

        locationService.updateLocationState(
                queueEntity.getLocationId(),
                new LocationChange.UpdateClient(
                        LocationState.Client.toModel(
                                clientEntity,
                                serviceEntities,
                                clientToChosenServiceEntities,
                                queueEntity
                        )
                )
        );
    }

    @Override
    public void returnClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }

        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientEntity clientEntity = client.get();

        QueueEntity queueEntity = queue.get();
        queueEntity.setClientId(null);
        queueRepo.save(queueEntity);

        List<ServiceEntity> serviceEntities = serviceRepo.findAllByLocationIdAndAssignedToClient(queueEntity.getLocationId(), clientId);
        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(clientId);

        locationService.updateLocationState(
                queueEntity.getLocationId(),
                new LocationChange.UpdateClient(
                        LocationState.Client.toModel(
                                clientEntity,
                                serviceEntities,
                                clientToChosenServiceEntities,
                                null
                        )
                )
        );
    }

    @Override
    public QueueStateForClient getQueueStateForClient(Localizer localizer, Long clientId) throws DescriptionException {
        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientEntity clientEntity = client.get();
        if (
                Objects.equals(clientEntity.getStatus(), ClientStatusEntity.Status.RESERVED.name()) &&
                        new Date().getTime() - clientEntity.getTotalTimestamp().getTime() > confirmationTime
        ) {
            throw new DescriptionException(localizer.getMessage(Message.CONFIRMATION_TIME_EXPIRED));
        }
        return QueueStateForClient.toModel(clientEntity);
    }

    @Override
    public QueueStateForClient confirmAccessKeyByClient(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException {
        ClientEntity clientEntity = checkAccessKey(localizer, clientId, accessKey);

        if (Objects.equals(clientEntity.getStatus(), ClientStatusEntity.Status.CONFIRMED.name())) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_ALREADY_CONFIRMED));
        }

        List<Integer> clientCodesInLocation = clientRepo.findAllClientCodesInLocation(clientEntity.getLocationId());

        clientEntity.setStatus(ClientStatusEntity.Status.CONFIRMED.name());
        clientEntity.setCode(CodeGenerator.generateCodeInLocation(clientCodesInLocation));

        clientRepo.save(clientEntity);

        List<ServiceEntity> serviceEntities = serviceRepo.findAllByLocationIdAndAssignedToClient(clientEntity.getLocationId(), clientId);
        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(clientId);

        locationService.updateLocationState(
                clientEntity.getLocationId(),
                new LocationChange.AddClient(
                        LocationState.Client.toModel(
                                clientEntity,
                                serviceEntities,
                                clientToChosenServiceEntities,
                                null
                        )
                )
        );

        return getQueueStateForClient(localizer, clientId);
    }

    @Override
    public void serveClient(Localizer localizer, String accessToken, Long queueId, Long clientId, ServeClientRequest serveClientRequest) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);

        Optional<Integer> minOrderNumber = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(clientId)
                .stream()
                .map(ClientToChosenServiceEntity::getOrderNumber)
                .min(Integer::compareTo);

        if (minOrderNumber.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }

        Integer min = minOrderNumber.get();

        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = clientToChosenServiceRepo.findAllByPrimaryKeyClientId(clientId);
        for (Long serviceId : serveClientRequest.getServices()) {
            Optional<ClientToChosenServiceEntity> clientToChosenService = clientToChosenServiceRepo.findByPrimaryKeyClientIdAndPrimaryKeyServiceId(
                    clientId,
                    serviceId
            );
            if (clientToChosenService.isEmpty()) {
                throw new DescriptionException(localizer.getMessage(Message.INCORRECT_SERVICES));
            }
            ClientToChosenServiceEntity clientToChosenServiceEntity = clientToChosenService.get();
            if (!Objects.equals(clientToChosenServiceEntity.getOrderNumber(), min)) {
                throw new DescriptionException(localizer.getMessage(Message.INCORRECT_SERVICES));
            }
            clientToChosenServiceRepo.delete(clientToChosenServiceEntity);
            clientToChosenServiceEntities.remove(clientToChosenServiceEntity);
        }

        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        queueEntity.setClientId(null);
        queueRepo.save(queueEntity);

        if (!clientToChosenServiceRepo.existsByPrimaryKeyClientId(clientId)) {
            clientRepo.deleteById(clientId);
            locationService.updateLocationState(
                    queueEntity.getLocationId(),
                    new LocationChange.DeleteClient(
                            clientId
                    )
            );
        } else {
            Optional<ClientEntity> client = clientRepo.findById(clientId);
            if (client.isPresent()) {
                ClientEntity clientEntity = client.get();
                clientEntity.setWaitTimestamp(new Date());
                clientRepo.save(clientEntity);

                List<ServiceEntity> serviceEntities = serviceRepo.findAllByLocationId(queueEntity.getLocationId());

                locationService.updateLocationState(
                        queueEntity.getLocationId(),
                        new LocationChange.UpdateClient(
                                LocationState.Client.toModel(
                                        clientEntity,
                                        serviceEntities,
                                        clientToChosenServiceEntities,
                                        null
                                )
                        )
                );
            }
        }
    }

    @Override
    public void notifyClient(Localizer localizer, String accessToken, Long queueId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        checkRightsInQueue(localizer, accessToken, queueId);
        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientEntity clientEntity = client.get();
        if (clientEntity.getPhone() == null) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_HAVE_PHONE));
        }
        smsService.send(clientEntity.getPhone(), localizer.getMessage(Message.PLEASE_GO_TO_SERVICE));
    }

    @Override
    public void deleteClientInLocation(Localizer localizer, String accessToken, Long locationId, Long clientId) throws DescriptionException, AccountIsNotAuthorizedException {
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId)) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
        Optional<QueueEntity> queue = queueRepo.findByClientId(clientId);
        if (queue.isPresent()) {
            QueueEntity queueEntity = queue.get();
            queueEntity.setClientId(null);
            queueRepo.save(queueEntity);
        }
        clientToChosenServiceRepo.deleteByPrimaryKeyClientId(clientId);
        clientRepo.deleteById(clientId);

        locationService.updateLocationState(
                locationId,
                new LocationChange.DeleteClient(
                        clientId
                )
        );
    }

    private void checkRightsInQueue(Localizer localizer, String accessToken, Long queueId) throws DescriptionException, AccountIsNotAuthorizedException {
        String accountEmail = accountService.getEmail(accessToken);
        Optional<QueueEntity> queue = queueRepo.findById(queueId);
        if (queue.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.QUEUE_DOES_NOT_EXIST));
        }
        QueueEntity queueEntity = queue.get();
        if (!rightsService.checkEmployeeRightsInLocation(localizer, accountEmail, queueEntity.getLocationId())) {
            throw new DescriptionException(localizer.getMessage(Message.YOU_DO_NOT_HAVE_RIGHTS_TO_PERFORM_OPERATION));
        }
    }

    private Map<Long, Integer> checkAddClientRequest(Localizer localizer, Long locationId, CreateClientRequest createClientRequest) throws DescriptionException {
        if (createClientRequest.getConfirmationRequired() && !PhoneHelper.phoneMatches(createClientRequest.getPhone())) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_PHONE));
        }
        return getServiceIdsToOrderNumbers(localizer, locationId, createClientRequest.getServiceIds(), createClientRequest.getServicesSequenceId());
    }

    private Map<Long, Integer> getServiceIdsToOrderNumbers(Localizer localizer, Long locationId, List<Long> serviceIds, Long servicesSequenceId) throws DescriptionException {
        boolean servicesChosen = serviceIds != null && !serviceIds.isEmpty();
        boolean servicesSequenceChosen = servicesSequenceId != null;
        if (servicesChosen == servicesSequenceChosen) {
            throw new DescriptionException(localizer.getMessage(Message.INCORRECT_REQUEST));
        }
        Map<Long, Integer> serviceIdsToOrderNumbers = new HashMap<>();
        if (servicesChosen) {
            for (Long serviceId : serviceIds) {
                if (!serviceRepo.existsByIdAndLocationId(serviceId, locationId)) {
                    throw new DescriptionException(localizer.getMessage(Message.ONE_OR_MORE_OF_CHOSEN_SERVICES_DO_NOT_EXIST_IN_LOCATION));
                }
                serviceIdsToOrderNumbers.put(serviceId, 1);
            }
        } else {
            if (!servicesSequenceRepo.existsByIdAndLocationId(servicesSequenceId, locationId)) {
                throw new DescriptionException(localizer.getMessage(Message.CHOSEN_SERVICES_SEQUENCE_DOES_NOT_EXIST_IN_LOCATION));
            }
            List<ServiceInServicesSequenceEntity> servicesInServicesSequenceEntities =
                    serviceInServicesSequenceRepo.findAllByPrimaryKeyServicesSequenceIdOrderByOrderNumberAsc(
                            servicesSequenceId
                    );
            for (ServiceInServicesSequenceEntity serviceInServicesSequenceEntity : servicesInServicesSequenceEntities) {
                serviceIdsToOrderNumbers.put(serviceInServicesSequenceEntity.getPrimaryKey().getServiceId(), serviceInServicesSequenceEntity.getOrderNumber());
            }
        }

        return serviceIdsToOrderNumbers;
    }

    private ClientEntity checkAccessKey(Localizer localizer, Long clientId, Integer accessKey) throws DescriptionException {
        Optional<ClientEntity> client = clientRepo.findById(clientId);
        if (client.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.CLIENT_DOES_NOT_EXIST));
        }
        ClientEntity clientEntity = client.get();
        if (!Objects.equals(clientEntity.getAccessKey(), accessKey)) {
            throw new DescriptionException(localizer.getMessage(Message.WRONG_ACCESS_KEY));
        }
        return clientEntity;
    }

    private String getLinkForClient(Localizer localizer, ClientEntity clientEntity, Long locationId) throws DescriptionException {
        Optional<LocationEntity> location = locationRepo.findById(locationId);
        if (location.isEmpty()) {
            throw new DescriptionException(localizer.getMessage(Message.LOCATION_DOES_NOT_EXIST));
        }
        return clientBaseUrl +
                "/client?client_id=" +
                clientEntity.getId() +
                "%26access_key=" +
                clientEntity.getAccessKey();
    }

    private ClientModel createClient(Localizer localizer, String accessToken, Long locationId, CreateClientRequest createClientRequest, Map<Long, Integer> serviceIdsToOrderNumbers) throws DescriptionException, AccountIsNotAuthorizedException {
        String phone = PhoneHelper.normalizePhoneForDatabase(createClientRequest.getPhone());
        Boolean confirmationRequired = createClientRequest.getConfirmationRequired();

        if (!confirmationRequired) {
            rightsService.checkEmployeeRightsInLocation(localizer, accountService.getEmail(accessToken), locationId);
        }

        if (phone != null) {
            Optional<ClientEntity> client = clientRepo.findByPhone(phone);
            if (client.isPresent()) {
                ClientEntity clientEntity = client.get();
                if (Objects.equals(clientEntity.getStatus(), ClientStatusEntity.Status.CONFIRMED.name())) {
                    throw new DescriptionException(localizer.getMessage(Message.CLIENT_WITH_THIS_PHONE_ALREADY_EXIST));
                }
                if (new Date().getTime() - clientEntity.getTotalTimestamp().getTime() < confirmationTime) {
                    throw new DescriptionException(
                            localizer.getMessage(
                                    Message.CLIENT_WITH_PHONE_RESERVED_START,
                                    phone,
                                    Message.CLIENT_WITH_PHONE_RESERVED_END
                            )
                    );
                } else {
                    clientToChosenServiceRepo.deleteByPrimaryKeyClientId(clientEntity.getId());
                    clientRepo.delete(clientEntity);
                }
            }
        }

        ClientEntity clientEntity;
        if (confirmationRequired) {
            clientEntity = clientRepo.save(
                    new ClientEntity(
                            null,
                            locationId,
                            phone,
                            null,
                            CodeGenerator.generateAccessKey(),
                            ClientStatusEntity.Status.RESERVED.name(),
                            new Date(),
                            new Date()
                    )
            );
        } else {
            List<Integer> clientCodesInLocation = clientRepo.findAllClientCodesInLocation(locationId);
            clientEntity = clientRepo.save(
                    new ClientEntity(
                            null,
                            locationId,
                            null,
                            CodeGenerator.generateCodeInLocation(clientCodesInLocation),
                            CodeGenerator.generateAccessKey(),
                            ClientStatusEntity.Status.CONFIRMED.name(),
                            new Date(),
                            new Date()
                    )
            );
        }

        List<ClientToChosenServiceEntity> clientToChosenServiceEntities = new ArrayList<>();
        for (Map.Entry<Long, Integer> serviceIdToOrderNumber : serviceIdsToOrderNumbers.entrySet()) {
            clientToChosenServiceEntities.add(
                    new ClientToChosenServiceEntity(
                            new ClientToChosenServiceEntity.PrimaryKey(
                                    clientEntity.getId(),
                                    serviceIdToOrderNumber.getKey()
                            ),
                            serviceIdToOrderNumber.getValue()
                    )
            );
        }
        clientToChosenServiceRepo.saveAll(clientToChosenServiceEntities);

        if (confirmationRequired) {
            smsService.send(
                    phone,
                    localizer.getMessageForClientConfirmation(getLinkForClient(localizer, clientEntity, locationId))
            );
        } else {
            List<ServiceEntity> serviceEntities = serviceRepo.findAllByLocationIdAndAssignedToClient(clientEntity.getLocationId(), clientEntity.getId());
            locationService.updateLocationState(
                    clientEntity.getLocationId(),
                    new LocationChange.AddClient(
                            LocationState.Client.toModel(
                                    clientEntity,
                                    serviceEntities,
                                    clientToChosenServiceEntities,
                                    null
                            )
                    )
            );
        }

        return ClientModel.toModel(clientEntity);
    }
}
